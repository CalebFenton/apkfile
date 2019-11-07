package org.cf.apkfile.apk;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.exception.ZipException;
import org.cf.apkfile.ParseException;
import org.cf.apkfile.analysis.ComplexityAnalyzer;
import org.cf.apkfile.dex.DexFile;
import org.cf.apkfile.dex.DexFileFactory;
import org.cf.apkfile.manifest.AndroidManifest;
import org.cf.apkfile.res.ResourceTableChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ApkFile {
    private static final transient Logger logger = LoggerFactory.getLogger(ApkFile.class);

    public static final Pattern CERTIFICATE_PATTERN = Pattern.compile("META-INF/[^.]+\\.(RSA|DSA|EC)", Pattern.CASE_INSENSITIVE);
    public static final Pattern DEX_PATTERN = Pattern.compile("\\.dex$", Pattern.CASE_INSENSITIVE);
    public static final Pattern ASSETS_PATTERN = Pattern.compile("^assets/");
    public static final Pattern RESOURCES_PATTERN = Pattern.compile("^res/");
    public static final Pattern RAW_RESOURCES_PATTERN = Pattern.compile("^res/raw/");
    public static final Pattern LIB_PATTERN = Pattern.compile("^lib/");
    public static final byte[] DEX_MAGIC = new byte[] { 0x64, 0x65, 0x78, 0x0A, 0x30, 0x33, };
    // 0x70 is min header length + 0x30 for the rest of the file
    private static final int MINIMUM_DEX_SIZE = 0x70 + 0x30;
    
    private final Map<String, FileHeader> entryNameToEntry;
    private final Map<String, DexFile> entryNameToDex;
    
    private AndroidManifest androidManifest;
    private Certificate certificate;
    private Resources resources;

    private final transient File theFile;
    private final transient ZipFile zipFile;

    private transient boolean skipParsingAndroidManifest;
    private transient boolean skipParsingResources;
    private transient boolean skipParsingDexFiles;
    private transient boolean useSimpleDexDetection;
    private transient DexFileFactory dexFactory;

    ApkFile(String apkPath, boolean parseCertificate) throws IOException, ZipException {
        zipFile = new ZipFile(apkPath);
        theFile = new File(apkPath);
        androidManifest = null;
        resources = null;
        entryNameToDex = new HashMap<>();
        entryNameToEntry = buildEntryNameToEntry(zipFile);

        if (parseCertificate) {
            try {
                parseCertificate();
            } catch (ParseException e) {
                logger.error("Exception parsing certificate", e);
            }
        } else {
            certificate = null;
        }
    }

    ApkFile parse() {
        if (!skipParsingResources) {
            try {
                parseResources();
            } catch (ParseException e) {
                logger.error("Error parsing resources", e);
            }
        }

        if (!skipParsingAndroidManifest) {
            ResourceTableChunk resourceTable = resources != null ? resources.getResourceTable() : null;
            try {
                parseAndroidManifest(resourceTable);
            } catch (ParseException e) {
                logger.error("Error parsing Android Manifest", e);
            }
        }

        if (!skipParsingDexFiles) {
            parseDexFiles();

            for (Map.Entry<String, DexFile> entry : entryNameToDex.entrySet()) {
                entry.getValue().analyze();
            }

            // Need to analyze all DEX files in case they reference each other
            ComplexityAnalyzer complexityAnalyzer = new ComplexityAnalyzer(entryNameToDex.values());
            complexityAnalyzer.analyze();
        }

        return this;
    }

    public Collection<FileHeader> getAllEntries() {
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

    public Map<String, FileHeader> getEntries(String entryNamePattern) {
        return getEntries(Pattern.compile(entryNamePattern));
    }

    public Map<String, FileHeader> getEntries(Pattern entryNamePattern) {
        return entryNameToEntry.entrySet().stream()
                .filter(e -> entryNamePattern.matcher(e.getKey()).find())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public FileHeader getEntry(String entryName) {
        Optional<Map.Entry<String, FileHeader>> entry = entryNameToEntry.entrySet().stream()
                .filter(e -> entryName.equals(e.getKey())).findFirst();

        return entry.map(Map.Entry::getValue).orElse(null);
    }

    public FileHeader getEntry(Pattern entryNamePattern) {
        Optional<Map.Entry<String, FileHeader>> entry = entryNameToEntry.entrySet().stream()
                .filter(e -> entryNamePattern.matcher(e.getKey()).matches()).findFirst();

        return entry.map(Map.Entry::getValue).orElse(null);
    }

    public Map<String, FileHeader> getEntryMap() {
        return entryNameToEntry;
    }

    public Resources getResources() {
        return resources;
    }

    public File getFile() {
        return theFile;
    }

    public InputStream getInputStream(String fileName) throws IOException {
        FileHeader fileHeader = zipFile.getFileHeader(fileName);
        return getInputStream(fileHeader);
    }

    public InputStream getInputStream(FileHeader fileHeader) throws IOException {
        return zipFile.getInputStream(fileHeader);
    }

    @Nullable
    public FileHeader getResourcesArscEntry() {
        return getEntry("resources.arsc");
    }

    @Nullable
    public FileHeader getAndroidManifestEntry() {
        return getEntry("AndroidManifest.xml");
    }

    public Map<String, FileHeader> getAssetsEntries() {
        return getEntries(ASSETS_PATTERN);
    }

    public Map<String, FileHeader> getResourcesEntries() {
        return getEntries(RESOURCES_PATTERN);
    }

    public Map<String, FileHeader> getRawResourcesEntries() {
        return getEntries(RAW_RESOURCES_PATTERN);
    }

    public Map<String, FileHeader> getLibEntries() {
        return getEntries(LIB_PATTERN);
    }

    public FileHeader getCertificateEntry() {
        return getEntry(CERTIFICATE_PATTERN);
    }

    public Map<String, FileHeader> getDexEntries() {
        return getEntries(DEX_PATTERN);
    }

    public void parseAndroidManifest(@Nullable ResourceTableChunk resourceTable) throws ParseException {
        if (androidManifest != null) {
            return;
        }

        FileHeader androidManifestEntry = getAndroidManifestEntry();
        if (androidManifestEntry == null) {
            throw new ParseException("No AndroidManifest found; invalid APK");
        }

        InputStream manifestStream;
        try {
            manifestStream = getInputStream(androidManifestEntry);
        } catch (IOException e) {
            throw new ParseException("Unable to stream Android Manifest", e);
        }

        try {
            androidManifest = new AndroidManifest(manifestStream, resourceTable);
        } catch (Exception e) {
            throw new ParseException("Failed to parse Android Manifest", e);
        }
    }

    public void parseDexFiles() {
        if (entryNameToDex.size() > 0) {
            return;
        }

        Map<String, InputStream> dexStreams = getDexSteams();
        for (Map.Entry<String, InputStream> entry : dexStreams.entrySet()) {
            String entryName = entry.getKey();
            InputStream dexStream = entry.getValue();
            try {
                entryNameToDex.put(entryName, dexFactory.build(dexStream));
            } catch (IOException e) {
                logger.error("Error parsing DEX: " + entryName, e);
            }
        }
    }

    public void parseResources() throws ParseException {
        if (resources != null) {
            return;
        }

        FileHeader resourcesEntry = getResourcesArscEntry();
        if (resourcesEntry == null) {
            throw new ParseException("No resources.arsc found; invalid APK");
        }

        InputStream resourcesStream;
        try {
            resourcesStream = getInputStream(resourcesEntry);
        } catch (IOException e) {
            throw new RuntimeException("Unable to open resource input stream: " + e);
        }

        try {
            resources = new Resources(resourcesStream);
        } catch (Exception e) {
            throw new ParseException("Failed to parse resources", e);
        }
    }

    public void parseCertificate() throws ParseException {
        if (certificate != null) {
            return;
        }

        FileHeader certEntry = getCertificateEntry();
        if (certEntry == null) {
            throw new ParseException("No certificate found; unsigned APK");
        }

        InputStream certStream;
        try {
            certStream = getInputStream(certEntry);
        } catch (IOException e) {
            throw new ParseException("Unable to open certificate input stream", e);
        }

        try {
            certificate = new Certificate(certStream);
        } catch (IOException | CertificateException | NoSuchAlgorithmException e) {
            throw new ParseException("Unable to parse signature: " + certEntry.getFileName(), e);
        }
    }

    ApkFile setDexFactory(DexFileFactory dexFactory) {
        this.dexFactory = dexFactory;
        return this;
    }

    ApkFile setSkipParsingAndroidManifest(boolean skipParsingAndroidManifest) {
        this.skipParsingAndroidManifest = skipParsingAndroidManifest;
        return this;
    }

    ApkFile setSkipParsingResources(boolean skipParsingResources) {
        this.skipParsingResources = skipParsingResources;
        return this;
    }

    ApkFile setSkipParsingDexFiles(boolean skipParsingDexFiles) {
        this.skipParsingDexFiles = skipParsingDexFiles;
        return this;
    }

    ApkFile setUseSimpleDexDetection(boolean useSimpleDexDetection) {
        this.useSimpleDexDetection = useSimpleDexDetection;
        return this;
    }

    private Map<String, InputStream> getDexSteams() {
        Map<String, InputStream> dexStreams = new HashMap<>();
        if (useSimpleDexDetection) {
            dexStreams = getDexEntries().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> {
                        try {
                            return zipFile.getInputStream(e.getValue());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                            return null;
                        }
                    }));
        } else {
            for (Map.Entry<String, FileHeader> entry : entryNameToEntry.entrySet()) {
                FileHeader ze = entry.getValue();
                if (ze.getUncompressedSize() < MINIMUM_DEX_SIZE) {
                    continue;
                }
                byte[] bytes = new byte[DEX_MAGIC.length];
                try {
                    InputStream is = zipFile.getInputStream(ze);
                    BufferedInputStream buf = new BufferedInputStream(is, bytes.length);
                    buf.read(bytes, 0, bytes.length);
                    buf.close();
                    if (Arrays.equals(DEX_MAGIC, bytes)) {
                        is = zipFile.getInputStream(ze);
                        dexStreams.put(entry.getKey(), is);
                    }
                    buf.close();
                } catch (Exception e) {
                    logger.warn("Unable to read entry: " + ze.getFileName(), e);
                }
            }
        }

        return dexStreams;
    }

    private static Map<String, FileHeader> buildEntryNameToEntry(ZipFile zipFile) throws ZipException {
        Map<String, FileHeader> entryNameToEntry = new HashMap<>();
        for (FileHeader fileHeader : zipFile.getFileHeaders()) {
            entryNameToEntry.put(fileHeader.getFileName(), fileHeader);
        }

        return entryNameToEntry;
    }
}
