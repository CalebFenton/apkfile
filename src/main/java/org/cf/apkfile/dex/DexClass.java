package org.cf.apkfile.dex;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.cf.apkfile.utils.Utils;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedMethod;
import org.jf.dexlib2.util.ReferenceUtil;
import org.pmw.tinylog.Logger;

import java.util.HashMap;
import java.util.Map;

public class DexClass {

    private final transient DexBackedClassDef classDef;
    private final transient boolean shortMethodSignatures;
    private final transient boolean filterSupport;

    private final TObjectIntMap<String> methodAccessorCounts;
    private final Map<String, DexMethod> methodSignatureToMethod;
    private final int annotationCount;
    private final int size;
    private final int fieldCount;
    private final int accessFlags;

    private int failedMethodCount = 0;

    DexClass(DexBackedClassDef classDef, boolean shortMethodSignatures, boolean filterSupport) {
        this.classDef = classDef;
        this.shortMethodSignatures = shortMethodSignatures;
        this.filterSupport = filterSupport;

        methodSignatureToMethod = new HashMap<>();
        methodAccessorCounts = new TObjectIntHashMap<>();
        annotationCount = classDef.getAnnotations().size();
        size = classDef.getSize();
        fieldCount = Utils.makeCollection(classDef.getFields()).size();
        accessFlags = classDef.getAccessFlags();

        analyze();
    }

    private void analyze() {
        for (DexBackedMethod dbm : classDef.getMethods()) {
            if (filterSupport && DexFile.isSupportClass(dbm.getDefiningClass())) {
                continue;
            }

            DexMethod dexMethod;
            try {
                dexMethod = new DexMethod(dbm, shortMethodSignatures);
            } catch (Exception e) {
                Logger.warn("Failed to analyze method: " + ReferenceUtil.getMethodDescriptor(dbm) + "; skipping", e);
                failedMethodCount += 1;
                continue;
            }
            String methodDescriptor = ReferenceUtil.getMethodDescriptor(dbm);
            String methodSignature = methodDescriptor.substring(methodDescriptor.indexOf("->") + 2);
            methodSignatureToMethod.put(methodSignature, dexMethod);
        }

        int[] methodAccessFlags = new int[methodSignatureToMethod.size()];
        int idx = 0;
        for (DexMethod dexMethod : methodSignatureToMethod.values()) {
            methodAccessFlags[idx] = dexMethod.getAccessFlags();
            idx++;
        }
        Utils.updateAccessorCounts(methodAccessorCounts, methodAccessFlags);
    }

    public int getAccessFlags() {
        return accessFlags;
    }

    public int getSize() {
        return size;
    }

    public int getAnnotationCount() {
        return annotationCount;
    }

    public DexBackedClassDef getClassDef() {
        return classDef;
    }

    public int getFieldCount() {
        return fieldCount;
    }

    public DexMethod getMethod(String methodSignature) {
        return methodSignatureToMethod.get(methodSignature);
    }

    public TObjectIntMap<String> getMethodAccessorCounts() {
        return methodAccessorCounts;
    }

    public Map<String, DexMethod> getMethodSignatureToMethod() {
        return methodSignatureToMethod;
    }

    public int getFailedMethodCount() {
        return failedMethodCount;
    }

}
