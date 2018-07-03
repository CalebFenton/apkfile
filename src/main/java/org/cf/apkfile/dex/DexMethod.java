package org.cf.apkfile.dex;


import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;

import org.cf.apkfile.utils.Utils;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.ReferenceType;
import org.jf.dexlib2.dexbacked.DexBackedMethod;
import org.jf.dexlib2.dexbacked.DexBackedMethodImplementation;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.reference.StringReference;
import org.jf.dexlib2.util.ReferenceUtil;

import java.lang.reflect.Modifier;

import javax.annotation.Nonnull;

public class DexMethod {

    private static final String[] API_PACKAGES = new String[]{"Landroid/", "Lcom/android/", "Lcom/google/", "Lcom/sec/android/", "Lcom/sun/", "Ldalvik/", "Lgov/", "Ljava/", "Ljavax/", "Ljunit/", "Llibcore/", "Lorg/apache/", "Lorg/ccil/", "Lorg/json/", "Lorg/kxml2/", "Lorg/spongycastle/", "Lorg/w3c/", "Lorg/xml/", "Lorg/xmlpull/", "Lsun/"};

    private final TObjectIntMap<MethodReference> apiCounts;
    private final TObjectIntMap<FieldReference> fieldReferenceCounts;
    private final transient DexBackedMethod method;
    private final TObjectIntMap<String> methodAccessors;
    private final TObjectIntMap<Opcode> opCounts;
    private final TObjectIntMap<StringReference> stringReferenceCounts;
    private final transient boolean fullMethodSignatures;

    private int annotationCount = 0;

    private int cyclomaticComplexity = 0;
    private int debugItemCount = 0;
    private int instructionCount = 0;
    private int registerCount = 0;
    private int tryCatchCount = 0;

    public DexMethod(DexBackedMethod method) {
        this(method, true);
    }

    public DexMethod(DexBackedMethod method, boolean fullMethodSignatures) {
        this.fullMethodSignatures = fullMethodSignatures;

        this.method = method;
        opCounts = new TObjectIntHashMap<>();
        apiCounts = new TObjectIntHashMap<>();
        stringReferenceCounts = new TObjectIntHashMap<>();
        fieldReferenceCounts = new TObjectIntHashMap<>();
        annotationCount = method.getAnnotations().size();
        if (method.getImplementation() != null) {
            analyze(method.getImplementation());
        }
        methodAccessors = buildAccessors(method.getAccessFlags());
    }

    private void analyze(@Nonnull DexBackedMethodImplementation implementation) {
        registerCount = implementation.getRegisterCount();
        tryCatchCount = implementation.getTryBlocks().size();
        debugItemCount = Utils.makeCollection(implementation.getDebugItems()).size();

        for (Instruction instruction : implementation.getInstructions()) {
            instructionCount++;
            Opcode op = instruction.getOpcode();
            opCounts.adjustOrPutValue(op, 1, 1);

            if (instruction instanceof ReferenceInstruction) {
                ReferenceInstruction refInstr = (ReferenceInstruction) instruction;
                if (op.referenceType == ReferenceType.METHOD || op.referenceType == ReferenceType.FIELD) {
                    boolean isApi = false;
                    for (String apiPackage : API_PACKAGES) {
                        String refStr = ReferenceUtil.getReferenceString(refInstr.getReference());
                        if (refStr.startsWith(apiPackage)) {
                            isApi = true;
                            break;
                        }
                        if (!isApi) {
                            continue;
                        }
                    }
                }

                switch (op.referenceType) {
                    case ReferenceType.METHOD:
                        MethodReference methodRef = (MethodReference) refInstr.getReference();
                        if (fullMethodSignatures) {
                            apiCounts.adjustOrPutValue(methodRef, 1, 1);
                        } else {
                            ShortMethodReference shortMethodRef = new ShortMethodReference(
                                    methodRef);
                            apiCounts.adjustOrPutValue(shortMethodRef, 1, 1);
                        }
                        break;
                    case ReferenceType.FIELD:
                        FieldReference fieldRef = (FieldReference) refInstr.getReference();
                        fieldReferenceCounts.adjustOrPutValue(fieldRef, 1, 1);
                        break;
                    case ReferenceType.STRING:
                        StringReference stringRef = (StringReference) refInstr.getReference();
                        stringReferenceCounts.adjustOrPutValue(stringRef, 1, 1);
                        break;
                }
            }
        }
    }


    public int getAnnotationCount() {
        return annotationCount;
    }

    public TObjectIntMap<MethodReference> getApiCounts() {
        return apiCounts;
    }

    public int getCyclomaticComplexity() {
        return cyclomaticComplexity;
    }

    public int getDebugItemCount() {
        return debugItemCount;
    }

    public TObjectIntMap<FieldReference> getFieldReferenceCounts() {
        return fieldReferenceCounts;
    }

    public int getInstructionCount() {
        return instructionCount;
    }

    public DexBackedMethod getMethod() {
        return method;
    }

    public TObjectIntMap<String> getMethodAccessors() {
        return methodAccessors;
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

    public void setCyclomaticComplexity(int cyclomaticComplexity) {
        this.cyclomaticComplexity = cyclomaticComplexity;
    }

    public String toString() {
        return "";
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
}
