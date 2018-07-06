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

    public DexFileFactory getDexFileFactory() {
        return dexFactory;
    }

    public ApkFileFactory skipParsingResources() {
        skipParsingResources = true;
        return this;
    }

    public ApkFileFactory useSimpleDexDetection() {
        useSimpleDexDetection = true;
        return this;
    }

    public ApkFileFactory skipParsingAndroidManifest() {
        skipParsingAndroidManifest = true;
        return this;
    }

    public ApkFileFactory skipParsingCertificate() {
        skipParsingCertificate = true;
        return this;
    }

    public ApkFileFactory skipParsingDexFiles() {
        skipParsingDexFiles = true;
        return this;
    }

}
