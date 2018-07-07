package org.cf.apkfile.apk;


import org.cf.apkfile.ParseException;
import org.cf.apkfile.dex.DexFileFactory;

import java.io.File;
import java.io.IOException;

public class ApkFileFactory {

    private boolean skipParsingAndroidManifest;
    private boolean skipParsingCertificate;
    private boolean skipParsingResources;
    private boolean skipParsingDexFiles;
    private boolean useSimpleDexDetection;
    private DexFileFactory dexFactory;

    public ApkFileFactory() {
        skipParsingResources = false;
        skipParsingAndroidManifest = false;
        skipParsingCertificate = false;
        useSimpleDexDetection = false;

        dexFactory = new DexFileFactory();
    }

    public ApkFile build(File file) throws IOException, ParseException {
        return build(file.getAbsolutePath());
    }

    public ApkFile build(String apkPath) throws IOException, ParseException {
        return new ApkFile(apkPath, !skipParsingCertificate)
                .setDexFactory(dexFactory)
                .setSkipParsingAndroidManifest(skipParsingAndroidManifest)
                .setSkipParsingResources(skipParsingResources)
                .setUseSimpleDexDetection(useSimpleDexDetection)
                .setSkipParsingDexFiles(skipParsingDexFiles)
                .parse();
    }

    /***
     * Used to access dex factory options.
     * @return the dex file factory instance for this apk file factory
     */
    public DexFileFactory getDexFileFactory() {
        return dexFactory;
    }

    /***
     * Skip parsing the APK's {@code resources.arsc} entry.
     * @return the apk file factory
     */
    public ApkFileFactory skipParsingResources() {
        skipParsingResources = true;
        return this;
    }

    /***
     * Only detect DEX files by looking for the {@code .dex} file extension.
     * Default behavior reads file headers and looks for magic bytes.
     * @return the apk file factory
     */
    public ApkFileFactory useSimpleDexDetection() {
        useSimpleDexDetection = true;
        return this;
    }

    /***
     * Skip parsing the APK's {@code AndroidManifest.xml} entry.
     * @return the apk file factory
     */
    public ApkFileFactory skipParsingAndroidManifest() {
        skipParsingAndroidManifest = true;
        return this;
    }

    /***
     * Skip parsing the APK's signing certificate in {@code META-INF/}
     * @return the apk file factory
     */
    public ApkFileFactory skipParsingCertificate() {
        skipParsingCertificate = true;
        return this;
    }

    /***
     * Skip all DEX file analysis.
     * @return the apk file factory
     */
    public ApkFileFactory skipParsingDexFiles() {
        skipParsingDexFiles = true;
        return this;
    }

}
