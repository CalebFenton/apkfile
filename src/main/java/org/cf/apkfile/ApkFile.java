package org.cf.apkfile;

import com.beust.jcommander.internal.Nullable;
import javafx.util.Pair;
import org.bouncycastle.cms.CMSException;
import org.cf.apkfile.apk.Certificate;
import org.cf.apkfile.apk.Resources;
import org.cf.apkfile.dex.DexFile;
import org.cf.apkfile.manifest.AndroidManifest;
import org.cf.apkfile.res.ResourceTableChunk;
import org.pmw.tinylog.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class ApkFile extends File {

    private static final Pattern CERTIFICATE_PATTERN = Pattern
            .compile("META-INF/[^\\.]+\\.(RSA|DSA|EC)", Pattern.CASE_INSENSITIVE);
    private static final byte[] DEX_MAGIC = new byte[]{0x64, 0x65, 0x78, 0x0A, 0x30, 0x33,};
    private static final Pattern DEX_PATTERN = Pattern.compile(".dex$", Pattern.CASE_INSENSITIVE);

    private final AndroidManifest androidManifest;
    private final Certificate certificate;
    private final Map<String, DexFile> entryNameToDex;
    private final Map<String, ZipEntry> entryNameToZipEntry;
    private final Resources resources;
    private final transient ZipFile zipFile;

    public ApkFile(File file) throws IOException, ParseException {
        this(file.getAbsolutePath());
    }

    public ApkFile(File file, boolean parseResources, boolean parseAndroidManifest, boolean parseCertificate) throws IOException, ParseException {
        this(file.getAbsolutePath(), parseResources, parseAndroidManifest, parseCertificate);
    }

    public ApkFile(String apkPath) throws IOException, ParseException {
        this(apkPath, true, true, true);
    }

    public ApkFile(String apkPath, boolean parseResources, boolean parseAndroidManifest, boolean parseCertificate) throws IOException, ParseException {
        super(apkPath);
        this.zipFile = new ZipFile(apkPath);
        entryNameToZipEntry = buildEntryNameToZipEntry(zipFile);

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

        entryNameToDex = loadDexFiles(true);
        for (Map.Entry<String, DexFile> entry : entryNameToDex.entrySet()) {
            entry.getValue().analyze();
        }
    }

    public void close() throws IOException {
        zipFile.close();
    }

    public Collection<ZipEntry> getAllEntries() {
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

    public Map<String, ZipEntry> getEntries(String entryNamePattern) {
        return getEntries(Pattern.compile(entryNamePattern));
    }

    public Map<String, ZipEntry> getEntries(Pattern entryNamePattern) {
        return entryNameToZipEntry.entrySet().stream()
                .filter(e -> entryNamePattern.matcher(e.getKey()).matches())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public ZipEntry getEntry(String entryName) {
        Optional<Map.Entry<String, ZipEntry>> entry = entryNameToZipEntry.entrySet().stream()
                .filter(e -> entryName.equals(e.getKey())).findFirst();
        return entry.map(Map.Entry::getValue).orElse(null);

    }

    public ZipEntry getEntry(Pattern entryNamePattern) {
        Optional<Map.Entry<String, ZipEntry>> entry = entryNameToZipEntry.entrySet().stream()
                .filter(e -> entryNamePattern.matcher(e.getKey()).matches()).findFirst();

        return entry.map(Map.Entry::getValue).orElse(null);
    }

    public Map<String, ZipEntry> getEntryMap() {
        return entryNameToZipEntry;
    }

    public Resources getResources() {
        return resources;
    }

    public ZipFile getZipFile() {
        return zipFile;
    }

    private AndroidManifest loadAndroidManifest(@Nullable ResourceTableChunk resourceTable) throws ParseException {
        ZipEntry manifestEntry = getEntry("AndroidManifest.xml");
        if (manifestEntry == null) {
            throw new ParseException("No AndroidManifest found; invalid APK");
        }

        InputStream manifestStream;
        try {
            manifestStream = zipFile.getInputStream(manifestEntry);
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
            for (Map.Entry<String, ZipEntry> entry : entryNameToZipEntry.entrySet()) {
                ZipEntry ze = entry.getValue();
                // 0x70 is the header length
                if (ze.getSize() < 0x70) {
                    continue;
                }
                byte[] bytes = new byte[6];
                try {
                    InputStream is = zipFile.getInputStream(ze);
                    BufferedInputStream buf = new BufferedInputStream(is, bytes.length);
                    buf.read(bytes, 0, bytes.length);
                    buf.close();
                    if (Arrays.equals(DEX_MAGIC, bytes)) {
                        //System.out.println("Found dex: " + ze.getName() + " " + Arrays.toString(bytes));
                        is = zipFile.getInputStream(ze);
                        dexStreams.put(entry.getKey(), is);
                    }
                    buf.close();
                } catch (Exception e) {
                    Logger.warn(e, "Unable to read DEX file: " + ze.getName());
                }
            }
        } else {
            dexStreams = getEntries(DEX_PATTERN).entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> {
                        try {
                            return zipFile.getInputStream(e.getValue());
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
        ZipEntry resourcesEntry = getEntry("resources.arsc");
        if (resourcesEntry == null) {
            throw new ParseException("No resources.arsc found; invalid APK");
        }

        InputStream resourcesStream;
        try {
            resourcesStream = zipFile.getInputStream(resourcesEntry);
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
        ZipEntry certEntry = getEntry(CERTIFICATE_PATTERN);
        if (certEntry == null) {
            throw new ParseException("No certificate found; unsigned APK");
        }

        InputStream certStream;
        try {
            certStream = zipFile.getInputStream(certEntry);
        } catch (IOException e) {
            throw new ParseException("No certificate found; unsigned APK", e);
        }

        try {
            return new Certificate(certStream);
        } catch (CMSException e) {
            throw new ParseException("Unable to parse signature: " + certEntry.getName(), e);
        }
    }

    private static Map<String, ZipEntry> buildEntryNameToZipEntry(
            ZipFile zipFile) throws IOException {
        Map<String, ZipEntry> entryNameToZipEntry = new HashMap<>();
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();
            entryNameToZipEntry.put(name, entry);
        }

        return entryNameToZipEntry;
    }
}
