package org.cf.apkfile;

import javafx.util.Pair;
import org.bouncycastle.cms.CMSException;
import org.cf.apkfile.apk.Certificate;
import org.cf.apkfile.apk.Resources;
import org.cf.apkfile.dex.DexFile;
import org.cf.apkfile.manifest.AndroidManifest;
import org.cf.apkfile.res.ResourceTableChunk;
import org.pmw.tinylog.Logger;

import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class ApkFile extends JarFile {

    public static final Pattern CERTIFICATE_PATTERN = Pattern
            .compile("META-INF/[^\\.]+\\.(RSA|DSA|EC)", Pattern.CASE_INSENSITIVE);
    public static final Pattern DEX_PATTERN = Pattern.compile(".dex$", Pattern.CASE_INSENSITIVE);
    public static final Pattern ASSETS_PATTERN = Pattern.compile("^assets/");
    public static final Pattern RESOURCES_PATTERN = Pattern.compile("^res/");
    public static final Pattern RAW_RESOURCES_PATTERN = Pattern.compile("%res/raw/");
    public static final Pattern LIB_PATTERN = Pattern.compile("^lib/");

    public static final byte[] DEX_MAGIC = new byte[]{0x64, 0x65, 0x78, 0x0A, 0x30, 0x33,};

    private final AndroidManifest androidManifest;
    private final Certificate certificate;
    private final Map<String, DexFile> entryNameToDex;
    private final Map<String, JarEntry> entryNameToEntry;
    private final Resources resources;
    private final transient File theFile;

    public ApkFile(File file) throws IOException, ParseException {
        this(file.getAbsolutePath());
    }

    public ApkFile(File file, boolean parseResources, boolean parseAndroidManifest, boolean parseCertificate) throws IOException, ParseException {
        this(file.getAbsolutePath(), true, parseResources, parseAndroidManifest, parseCertificate);
    }

    public ApkFile(String apkPath) throws IOException, ParseException {
        this(apkPath, true, true, true, true);
    }

    public ApkFile(String apkPath, boolean analyzeDexMagic, boolean parseResources, boolean parseAndroidManifest, boolean parseCertificate) throws IOException, ParseException {
        super(apkPath, parseCertificate);
        this.theFile = new File(apkPath);
        entryNameToEntry = buildEntryNameToEntry(this);

        if (parseCertificate) {
            certificate = parseCertificate();
        } else {
            certificate = null;
        }

        if (parseResources) {
            resources = parseResources();
        } else {
            resources = null;
        }

        if (parseAndroidManifest) {
            ResourceTableChunk resourceTable = resources != null ? resources.getResourceTable() : null;
            androidManifest = loadAndroidManifest(resourceTable);
        } else {
            androidManifest = null;
        }

        entryNameToDex = loadDexFiles(analyzeDexMagic);
        for (Map.Entry<String, DexFile> entry : entryNameToDex.entrySet()) {
            entry.getValue().analyze();
        }
    }

    public Collection<JarEntry> getAllEntries() {
        return getEntryMap().values();
    }

    public AndroidManifest getAndroidManifest() {
        return androidManifest;
    }

    public Certificate getCertificate() {
        return certificate;
    }

    public Map<String, DexFile> getDexFiles() {
        return entryNameToDex;
    }

    public Map<String, JarEntry> getEntries(String entryNamePattern) {
        return getEntries(Pattern.compile(entryNamePattern));
    }

    public Map<String, JarEntry> getEntries(Pattern entryNamePattern) {
        return entryNameToEntry.entrySet().stream()
                .filter(e -> entryNamePattern.matcher(e.getKey()).find())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public JarEntry getEntry(String entryName) {
        Optional<Map.Entry<String, JarEntry>> entry = entryNameToEntry.entrySet().stream()
                .filter(e -> entryName.equals(e.getKey())).findFirst();
        return entry.map(Map.Entry::getValue).orElse(null);

    }

    public JarEntry getEntry(Pattern entryNamePattern) {
        Optional<Map.Entry<String, JarEntry>> entry = entryNameToEntry.entrySet().stream()
                .filter(e -> entryNamePattern.matcher(e.getKey()).matches()).findFirst();

        return entry.map(Map.Entry::getValue).orElse(null);
    }

    public Map<String, JarEntry> getEntryMap() {
        return entryNameToEntry;
    }

    public Resources getResources() {
        return resources;
    }

    public File getFile() {
        return theFile;
    }

    @Nullable
    public JarEntry getResourcesArscEntry() {
        return getEntry("resources.arsc");
    }

    @Nullable
    public JarEntry getAndroidManifestEntry() {
        return getEntry("AndroidManifest.xml");
    }

    public Map<String, JarEntry> getAssetsEntries() {
        return getEntries(ASSETS_PATTERN);
    }

    public Map<String, JarEntry> getResourcesEntries() {
        return getEntries(RESOURCES_PATTERN);
    }

    public Map<String, JarEntry> getRawResourcesEntries() {
        return getEntries(RAW_RESOURCES_PATTERN);
    }

    public Map<String, JarEntry> getLibEntries() {
        return getEntries(LIB_PATTERN);
    }

    public JarEntry getCertificateEntry() {
        return getEntry(CERTIFICATE_PATTERN);
    }

    public Map<String, JarEntry> getDexEntries() {
        return getEntries(DEX_PATTERN);
    }

    private AndroidManifest loadAndroidManifest(@Nullable ResourceTableChunk resourceTable) throws ParseException {
        JarEntry manifestEntry = getAndroidManifestEntry();
        if (manifestEntry == null) {
            throw new ParseException("No AndroidManifest found; invalid APK");
        }

        InputStream manifestStream;
        try {
            manifestStream = getInputStream(manifestEntry);
        } catch (IOException e) {
            throw new ParseException("Unable to stream AndroidManifest", e);
        }

        try {
            return new AndroidManifest(manifestStream, resourceTable);
        } catch (Exception e) {
            throw new ParseException("Failed to parse manifest", e);
        }
    }

    private Map<String, DexFile> loadDexFiles(boolean analyzeMagic) {
        Map<String, InputStream> dexStreams = new HashMap<>();
        if (analyzeMagic) {
            for (Map.Entry<String, JarEntry> entry : entryNameToEntry.entrySet()) {
                JarEntry ze = entry.getValue();
                // 0x70 is the header length
                if (ze.getSize() < 0x70) {
                    continue;
                }
                byte[] bytes = new byte[6];
                try {
                    InputStream is = getInputStream(ze);
                    BufferedInputStream buf = new BufferedInputStream(is, bytes.length);
                    buf.read(bytes, 0, bytes.length);
                    buf.close();
                    if (Arrays.equals(DEX_MAGIC, bytes)) {
                        //System.out.println("Found dex: " + ze.getName() + " " + Arrays.toString(bytes));
                        is = getInputStream(ze);
                        dexStreams.put(entry.getKey(), is);
                    }
                    buf.close();
                } catch (Exception e) {
                    Logger.warn(e, "Unable to read DEX file: " + ze.getName());
                }
            }
        } else {
            dexStreams = getDexEntries().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> {
                        try {
                            return getInputStream(e.getValue());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                            return null;
                        }
                    }));
        }

        return dexStreams.entrySet().stream().map(e -> {
            try {
                return new Pair<>(e.getKey(), new DexFile(e.getValue()));
            } catch (IOException e1) {
                e1.printStackTrace();
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    private Resources parseResources() throws ParseException {
        JarEntry resourcesEntry = getResourcesArscEntry();
        if (resourcesEntry == null) {
            throw new ParseException("No resources.arsc found; invalid APK");
        }

        InputStream resourcesStream;
        try {
            resourcesStream = getInputStream(resourcesEntry);
        } catch (IOException e) {
            throw new RuntimeException("Unable to stream resources: " + e);
        }

        try {
            return new Resources(resourcesStream);
        } catch (Exception e) {
            throw new ParseException("Failed to parse resources", e);
        }
    }

    private Certificate parseCertificate() throws ParseException {
        JarEntry certEntry = getCertificateEntry();
        if (certEntry == null) {
            throw new ParseException("No certificate found; unsigned APK");
        }

        InputStream certStream;
        try {
            certStream = getInputStream(certEntry);
        } catch (IOException e) {
            throw new ParseException("No certificate found; unsigned APK", e);
        }

        try {
            return new Certificate(certStream);
        } catch (CMSException e) {
            throw new ParseException("Unable to parse signature: " + certEntry.getName(), e);
        }
    }

    private static Map<String, JarEntry> buildEntryNameToEntry(
            JarFile jarFile) throws IOException {
        Map<String, JarEntry> entryNameToEntry = new HashMap<>();
        Enumeration<? extends JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();
            entryNameToEntry.put(name, entry);
        }

        return entryNameToEntry;
    }
}
