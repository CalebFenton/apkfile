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

    /***
     * Don't include method parameter and return types in framework API counts.
     * This means if multiple methods have the same name, they'll be included in the same count.
     * @return the dex file factory
     */
    public DexFileFactory useShortMethodSignatures() {
        shortMethodSignatures = true;
        return this;
    }

    /***
     * Generate nGrams of method opcodes
     * @return the dex file factory
     */
    public DexFileFactory generateNGrams() {
        generateNGrams = true;
        return this;
    }

    /***
     * Don't analyze local Android support library methods or include them in API counts.
     * @return the dex file factory
     */
    public DexFileFactory filterSupportClasses() {
        filterSupportClasses = true;
        return this;
    }

    /***
     * Specify the order of the nGrams. This is only used if {@link #generateNGrams} is set.
     * @param n
     * @return the dex file factory
     */
    public DexFileFactory nGramSize(int n) {
        nGramSize = n;
        return this;
    }

}
