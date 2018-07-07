package org.cf.apkfile.dex;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.cf.apkfile.analysis.EntropyCalculatingInputStream;
import org.cf.apkfile.utils.Utils;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.pmw.tinylog.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DexFile {

    private static final transient String SUPPORT_PACKAGE = "Landroid/support/";

    static final transient int TARGET_API = 39;

    private final transient Set<String> LOCAL_CLASS_PATHS;
    private transient DexBackedDexFile dexFile;
    private final transient InputStream dexStream;

    private final Map<String, DexClass> classPathToClass;
    private final TObjectIntMap<String> classAccessorCounts;
    private final Map<String, DexMethod> methodDescriptorToMethod;

    private int failedClassCount = 0;
    private double entropy = 0.0D;
    private double perplexity = 0.0D;
    private long size = 0;

    private transient boolean shortMethodSignatures;
    private transient boolean filterSupportClasses;
    private transient boolean generateNGrams;
    private transient int nGramSize;

    DexFile(InputStream dexStream) throws IOException {
        this.dexStream = dexStream;

        classPathToClass = new HashMap<>();
        classAccessorCounts = new TObjectIntHashMap<>();
        methodDescriptorToMethod = new HashMap<>();

        LOCAL_CLASS_PATHS = new HashSet<>();
    }

    public double getEntropy() {
        return entropy;
    }

    public long getSize() {
        return size;
    }

    public boolean isLocalClass(String classPath) {
        return LOCAL_CLASS_PATHS.contains(classPath);
    }

    private boolean isLocalOrSupportClass(String classPath) {
        return isLocalClass(classPath) || isSupportClass(classPath);
    }

    public static boolean isSupportClass(String classPath) {
        return classPath.startsWith(SUPPORT_PACKAGE);
    }

    public void analyze() {
        for (DexBackedClassDef classDef : dexFile.getClasses()) {
            String classPath = classDef.getType();
            if (filterSupportClasses && isSupportClass(classPath)) {
                continue;
            }

            DexClass dexClass;
            try {
                dexClass = new DexClass(classDef, shortMethodSignatures, filterSupportClasses, generateNGrams, nGramSize);
            } catch (Exception e) {
                Logger.warn("Failed to analyze class: " + classDef.getType() + "; skipping", e);
                failedClassCount++;
                continue;
            }
            classPathToClass.put(classPath, dexClass);

            /*
             * The Framework API and field reference counts may include references to local classes
             * because it's possible to locally define some framework classes in the APK (though not
             * protected packages like Ljava). In order to ensure counts accurately represent
             * true framework references, remove any known local classes.
             */
            for (DexMethod dexMethod : dexClass.getMethodSignatureToMethod().values()) {
                methodDescriptorToMethod.put(dexMethod.toString(), dexMethod);

                if (filterSupportClasses) {
                    dexMethod.getFrameworkApiCounts().keySet()
                            .removeIf(k -> isLocalOrSupportClass(Utils.getComponentBase(k.getDefiningClass())));
                    dexMethod.getFrameworkFieldReferenceCounts().keySet()
                            .removeIf(k -> isLocalOrSupportClass(Utils.getComponentBase(k.getDefiningClass())));
                } else {
                    dexMethod.getFrameworkApiCounts().keySet()
                            .removeIf(k -> isLocalNonSupportClass(Utils.getComponentBase(k.getDefiningClass())));
                    dexMethod.getFrameworkFieldReferenceCounts().keySet()
                            .removeIf(k -> isLocalNonSupportClass(Utils.getComponentBase(k.getDefiningClass())));
                }
            }
        }

        int[] classAccessFlags = new int[classPathToClass.size()];
        int idx = 0;
        for (DexClass dexClass : classPathToClass.values()) {
            classAccessFlags[idx] = dexClass.getAccessFlags();
            idx++;
        }
        Utils.updateAccessorCounts(classAccessorCounts, classAccessFlags);
    }

    public TObjectIntMap<String> getClassAccessorCounts() {
        return classAccessorCounts;
    }

    public int getFailedClassCount() {
        return failedClassCount;
    }

    public double getPerplexity() {
        return perplexity;
    }

    public DexClass getClass(String classPath) {
        return classPathToClass.get(classPath);
    }

    public Map<String, DexClass> getClassPathToClass() {
        return classPathToClass;
    }

    public Map<String, DexMethod> getMethodDescriptorToMethod() {
        return methodDescriptorToMethod;
    }

    public DexMethod getMethod(String methodSignature) {
        return methodDescriptorToMethod.get(methodSignature);
    }

    public DexBackedDexFile getDexFile() {
        return dexFile;
    }

    DexFile parse() throws IOException {
        EntropyCalculatingInputStream bis = new EntropyCalculatingInputStream(dexStream);
        dexFile = DexBackedDexFile.fromInputStream(Opcodes.forApi(TARGET_API), bis);
        entropy = bis.entropy();
        perplexity = bis.perplexity();
        size = bis.total();
        cacheLocalClasses(dexFile);

        return this;
    }

    DexFile setShortMethodSignatures(boolean shortMethodSignatures) {
        this.shortMethodSignatures = shortMethodSignatures;
        return this;
    }

    DexFile setFilterSupportClasses(boolean filterSupportClasses) {
        this.filterSupportClasses = filterSupportClasses;
        return this;
    }

    DexFile setGenerateNGrams(boolean generateNGrams) {
        this.generateNGrams = generateNGrams;
        return this;
    }

    DexFile setNGramSize(int nGramSize) {
        this.nGramSize = nGramSize;
        return this;
    }

    private boolean isLocalNonSupportClass(String classPath) {
        return !isSupportClass(classPath) && isLocalClass(classPath);
    }

    private synchronized void cacheLocalClasses(DexBackedDexFile dexFile) {
        /*
         * Must collect all local classes before any analysis because an API method is defined as
         * any non-local method. In multi-dex situations, there many be many API calls which are not
         * local to a single DEX.
         */
        for (DexBackedClassDef classDef : dexFile.getClasses()) {
            String classPath = classDef.getType();
            LOCAL_CLASS_PATHS.add(classPath);
        }
    }

}
