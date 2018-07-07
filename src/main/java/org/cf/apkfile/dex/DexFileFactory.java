package org.cf.apkfile.dex;

import java.io.IOException;
import java.io.InputStream;

public class DexFileFactory {

    private boolean shortMethodSignatures;
    private boolean filterSupportClasses;
    private boolean generateNGrams;
    private int nGramSize;

    public DexFileFactory() {
        shortMethodSignatures = false;
        filterSupportClasses = false;
        generateNGrams = false;
        nGramSize = 3;
    }

    public DexFile build(InputStream dexStream) throws IOException {
        return new DexFile(dexStream)
                .setShortMethodSignatures(shortMethodSignatures)
                .setFilterSupportClasses(filterSupportClasses)
                .setGenerateNGrams(generateNGrams)
                .setNGramSize(nGramSize)
                .parse();
    }

    public DexFileFactory useShortMethodSignatures() {
        shortMethodSignatures = true;
        return this;
    }

    public DexFileFactory generateNGrams() {
        generateNGrams = true;
        return this;
    }

    public DexFileFactory filterSupportClasses() {
        filterSupportClasses = true;
        return this;
    }

    public DexFileFactory nGramSize(int n) {
        nGramSize = n;
        return this;
    }

}
