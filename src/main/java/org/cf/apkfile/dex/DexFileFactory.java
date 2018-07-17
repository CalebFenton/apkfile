package org.cf.apkfile.dex;

import java.io.IOException;
import java.io.InputStream;

public class DexFileFactory {

    private boolean shortMethodSignatures;
    private boolean filterSupportClasses;

    public DexFileFactory() {
        shortMethodSignatures = false;
        filterSupportClasses = false;
    }

    public DexFile build(InputStream dexStream) throws IOException {
        return new DexFile(dexStream)
                .setShortMethodSignatures(shortMethodSignatures)
                .setFilterSupportClasses(filterSupportClasses)
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
     * Don't analyze local Android support library methods or include them in API counts.
     * @return the dex file factory
     */
    public DexFileFactory filterSupportClasses() {
        filterSupportClasses = true;
        return this;
    }

}
