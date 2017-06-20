package org.cf.apkfile.dex;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import org.cf.apkfile.utils.Utils;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.reference.StringReference;
import org.jf.dexlib2.util.ReferenceUtil;
import org.pmw.tinylog.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DexFile {

    private static final transient Set<String> localClasses = new HashSet<>();

    private final TObjectIntMap<MethodReference> apiCounts;
    private final TObjectIntMap<String> classAccessorCounts;
    private final Map<String, DexClass> classPathToClass;
    private final TObjectIntMap<FieldReference> fieldReferenceCounts;
    private final TObjectIntMap<String> methodAccessorCounts;
    private final Map<String, DexMethod> methodDescriptorToMethod;
    private final TObjectIntMap<Opcode> opCounts;
    private final TObjectIntMap<StringReference> stringReferenceCounts;

    private int annotationCount = 0;
    private float cyclomaticComplexity = 0.0f;
    private int debugItemCount = 0;
    private int fieldCount = 0;
    private int instructionCount = 0;
    private int registerCount = 0;
    private int tryCatchCount = 0;
    private int failedClasses;

    private final transient DexBackedDexFile dexFile;

    public DexFile(InputStream is) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(is);
        dexFile = DexBackedDexFile.fromInputStream(Opcodes.forApi(39), bis);
        classPathToClass = new HashMap<>();
        methodDescriptorToMethod = new HashMap<>();
        opCounts = new TObjectIntHashMap<>();
        apiCounts = new TObjectIntHashMap<>();
        stringReferenceCounts = new TObjectIntHashMap<>();
        fieldReferenceCounts = new TObjectIntHashMap<>();
        methodAccessorCounts = new TObjectIntHashMap<>();
        classAccessorCounts = new TObjectIntHashMap<>();
        loadLocalClasses(dexFile);
    }

    private static synchronized void loadLocalClasses(DexBackedDexFile dexFile) {
        /*
         * Must collect all local classes before any analysis because an API method is defined as
         * any non-local method. In multi-dex situations, there many be many API calls which are not
         * local to a single DEX.
         */
        for (DexBackedClassDef classDef : dexFile.getClasses()) {
            String classPath = classDef.getType();
            localClasses.add(classPath);
        }
    }

    private boolean isLocalNonSupportClass(String classPath) {
        if (classPath.startsWith("Landroid/support/")) {
            return false;
        }
        return localClasses.contains(classPath);
    }
    public void analyze() {
        for (DexBackedClassDef classDef : dexFile.getClasses()) {
            String classPath = classDef.getType();
            DexClass dexClass;
            try {
                dexClass = new DexClass(classDef);
                failedClasses++;
            } catch (Exception e) {
                Logger.warn("Failed to analyze class: " + classDef.getType() + "; skipping", e);
                continue;
            }
            classPathToClass.put(classPath, dexClass);

            for (DexMethod method : dexClass.getMethodSignatureToMethod().values()) {
                String methodDescriptor = ReferenceUtil.getMethodDescriptor(method.getMethod());
                methodDescriptorToMethod.put(methodDescriptor, method);
            }

            /*
             * We want API counts here, not method calls to local classes.
             * In Dalvik, you can reference a method or field of a parent class by referring to
             * the child class. This means, to get *true* API fields and method calls, you'd
             * need to map method calls to parent methods up the class hierarchy of Android
             * framework classes. This is computationally expensive and there are multiple
             * framework versions. As an approximation, remove field and method references
             * to local, non-support classes.
            */
            dexClass.getApiCounts().keySet()
                    .removeIf(k -> isLocalNonSupportClass(getComponentBase(k.getDefiningClass())));
            dexClass.getFieldReferenceCounts().keySet()
                    .removeIf(k -> isLocalNonSupportClass(getComponentBase(k.getDefiningClass())));
            for (DexMethod dexMethod : dexClass.getMethodSignatureToMethod().values()) {
                dexMethod.getApiCounts().keySet().removeIf(
                        k -> isLocalNonSupportClass(getComponentBase(k.getDefiningClass())));
                dexMethod.getFieldReferenceCounts().keySet().removeIf(
                        k -> isLocalNonSupportClass(getComponentBase(k.getDefiningClass())));
            }

            Utils.rollUp(opCounts, dexClass.getOpCounts());
            Utils.rollUp(apiCounts, dexClass.getApiCounts());
            Utils.rollUp(stringReferenceCounts, dexClass.getStringReferenceCounts());
            Utils.rollUp(fieldReferenceCounts, dexClass.getFieldReferenceCounts());
            Utils.rollUp(methodAccessorCounts, dexClass.getMethodAccessorCounts());
            Utils.rollUp(classAccessorCounts, dexClass.getClassAccessors());
            fieldCount += dexClass.getFieldCount();
            annotationCount += dexClass.getAnnotationCount();
            registerCount += dexClass.getRegisterCount();
            instructionCount += dexClass.getInstructionCount();
            tryCatchCount += dexClass.getTryCatchCount();
            debugItemCount += dexClass.getDebugItemCount();
            cyclomaticComplexity += dexClass.getCyclomaticComplexity();
        }
        if (!classPathToClass.isEmpty()) {
            cyclomaticComplexity /= classPathToClass.size();
        }
    }

    private String getComponentBase(String classDescriptor) {
        /* Some class references for method calls look like:
         * [Lcom/google/android/gms/internal/zzvk$zza$zza;->clone()Ljava/lang/Object;
         * but the '[' form of the class isn't in local classes. Strip it out to get 'base' class
        */
        int index = 0;
        while (classDescriptor.charAt(index) == '[') {
            index += 1;
        }
        if (index == 0) {
            return classDescriptor;
        } else {
            return classDescriptor.substring(index);
        }
    }

    public int getAnnotationCount() {
        return annotationCount;
    }

    public TObjectIntMap<MethodReference> getApiCounts() {
        return apiCounts;
    }

    public DexClass getClass(String classPath) {
        return classPathToClass.get(classPath);
    }

    public TObjectIntMap<String> getClassAccessorCounts() {
        return classAccessorCounts;
    }

    public Map<String, DexClass> getClassPathToClass() {
        return classPathToClass;
    }

    public float getCyclomaticComplexity() {
        return cyclomaticComplexity;
    }

    public int getDebugItemCount() {
        return debugItemCount;
    }

    public DexBackedDexFile getDexFile() {
        return dexFile;
    }

    public int getFieldCount() {
        return fieldCount;
    }

    public TObjectIntMap<FieldReference> getFieldReferenceCounts() {
        return fieldReferenceCounts;
    }

    public int getInstructionCount() {
        return instructionCount;
    }

    public DexMethod getMethod(String methodDescriptor) {
        return methodDescriptorToMethod.get(methodDescriptor);
    }

    public TObjectIntMap<String> getMethodAccessorCounts() {
        return methodAccessorCounts;
    }

    public Map<String, DexMethod> getMethodDescriptorToMethod() {
        return methodDescriptorToMethod;
    }

    public TObjectIntMap<Opcode> getOpCounts() {
        return opCounts;
    }

    public int getRegisterCount() {
        return registerCount;
    }

    public TObjectIntMap<StringReference> getStringReferenceCounts() {
        return stringReferenceCounts;
    }

    public int getTryCatchCount() {
        return tryCatchCount;
    }
}
