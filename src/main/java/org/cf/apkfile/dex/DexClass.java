package org.cf.apkfile.dex;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.cf.apkfile.utils.Utils;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedMethod;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.reference.StringReference;
import org.jf.dexlib2.util.ReferenceUtil;
import org.pmw.tinylog.Logger;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class DexClass {

    private final TObjectIntMap<MethodReference> apiCounts;
    private final TObjectIntMap<String> classAccessors;
    private final transient DexBackedClassDef classDef;
    private final TObjectIntMap<FieldReference> fieldReferenceCounts;
    private final TObjectIntMap<String> methodAccessorCounts;
    private final Map<String, DexMethod> methodSignatureToMethod;
    private final TObjectIntMap<Opcode> opCounts;
    private final TObjectIntMap<StringReference> stringReferenceCounts;
    private final boolean fullMethodSignatures;

    private int annotationCount = 0;
    private double cyclomaticComplexity = 0.0D;
    private int debugItemCount = 0;
    private int fieldCount = 0;
    private int instructionCount = 0;
    private int registerCount = 0;
    private int tryCatchCount = 0;
    private int failedMethods = 0;

    public DexClass(DexBackedClassDef classDef) {
        this(classDef, true);
    }

    public DexClass(DexBackedClassDef classDef, boolean fullMethodSignatures) {
        this.fullMethodSignatures = fullMethodSignatures;

        this.classDef = classDef;
        methodSignatureToMethod = new HashMap<>();
        opCounts = new TObjectIntHashMap<>();
        apiCounts = new TObjectIntHashMap<>();
        stringReferenceCounts = new TObjectIntHashMap<>();
        fieldReferenceCounts = new TObjectIntHashMap<>();
        methodAccessorCounts = new TObjectIntHashMap<>();
        analyze();
        classAccessors = buildAccessors(classDef.getAccessFlags());
    }


    private void analyze() {
        fieldCount = Utils.makeCollection(classDef.getFields()).size();
        for (DexBackedMethod dbm : classDef.getMethods()) {
            DexMethod dexMethod;
            try {
                dexMethod = new DexMethod(dbm, fullMethodSignatures);
                failedMethods += 1;
            } catch (Exception e) {
                Logger.warn("Failed to analyze method: " + ReferenceUtil.getMethodDescriptor(dbm) + "; skipping", e);
                continue;
            }
            String methodDescriptor = ReferenceUtil.getMethodDescriptor(dbm);
            String methodSignature = methodDescriptor.split("->", 2)[1];
            methodSignatureToMethod.put(methodSignature, dexMethod);

            Utils.rollUp(opCounts, dexMethod.getOpCounts());
            Utils.rollUp(apiCounts, dexMethod.getApiCounts());
            Utils.rollUp(stringReferenceCounts, dexMethod.getStringReferenceCounts());
            Utils.rollUp(fieldReferenceCounts, dexMethod.getFieldReferenceCounts());
            Utils.rollUp(methodAccessorCounts, dexMethod.getMethodAccessors());

            annotationCount += dexMethod.getAnnotationCount();
            registerCount += dexMethod.getRegisterCount();
            instructionCount += dexMethod.getInstructionCount();
            tryCatchCount += dexMethod.getTryCatchCount();
            debugItemCount += dexMethod.getDebugItemCount();
            cyclomaticComplexity += dexMethod.getCyclomaticComplexity();
        }
        if (!methodSignatureToMethod.isEmpty()) {
            cyclomaticComplexity /= methodSignatureToMethod.size();
        }
    }

    public int getAnnotationCount() {
        return annotationCount;
    }

    public TObjectIntMap<MethodReference> getApiCounts() {
        return apiCounts;
    }

    public TObjectIntMap<String> getClassAccessors() {
        return classAccessors;
    }

    public DexBackedClassDef getClassDef() {
        return classDef;
    }

    public double getCyclomaticComplexity() {
        return cyclomaticComplexity;
    }

    public int getDebugItemCount() {
        return debugItemCount;
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

    public DexMethod getMethod(String methodSignature) {
        return methodSignatureToMethod.get(methodSignature);
    }

    public TObjectIntMap<String> getMethodAccessorCounts() {
        return methodAccessorCounts;
    }

    public Map<String, DexMethod> getMethodSignatureToMethod() {
        return methodSignatureToMethod;
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

    private static TObjectIntMap<String> buildAccessors(int accessFlags) {
        TObjectIntMap<String> map = new TObjectIntHashMap<>();
        map.put("public", Modifier.isPublic(accessFlags) ? 1 : 0);
        map.put("protected", Modifier.isProtected(accessFlags) ? 1 : 0);
        map.put("private", Modifier.isPrivate(accessFlags) ? 1 : 0);
        map.put("final", Modifier.isFinal(accessFlags) ? 1 : 0);
        map.put("interface", Modifier.isInterface(accessFlags) ? 1 : 0);
        map.put("native", Modifier.isNative(accessFlags) ? 1 : 0);
        map.put("static", Modifier.isStatic(accessFlags) ? 1 : 0);
        map.put("strict", Modifier.isStrict(accessFlags) ? 1 : 0);
        map.put("synchronized", Modifier.isSynchronized(accessFlags) ? 1 : 0);
        map.put("transient", Modifier.isTransient(accessFlags) ? 1 : 0);
        map.put("volatile", Modifier.isVolatile(accessFlags) ? 1 : 0);
        map.put("abstract", Modifier.isAbstract(accessFlags) ? 1 : 0);
        return map;
    }

    public int getFailedMethods() {
        return failedMethods;
    }
}
