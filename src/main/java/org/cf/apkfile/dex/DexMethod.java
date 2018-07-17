package org.cf.apkfile.dex;


import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.cf.apkfile.analysis.DexReaderEntropyCalculator;
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

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

public class DexMethod {

    private static final String[] API_PACKAGES = new String[] {
            "Landroid/", "Lcom/android/", "Lcom/google/", "Lcom/sec/android/", "Lcom/sun/", "Ldalvik/", "Lgov/",
            "Ljava/", "Ljavax/", "Ljunit/", "Llibcore/", "Lorg/apache/", "Lorg/ccil/", "Lorg/json/",
            "Lorg/kxml2/", "Lorg/spongycastle/", "Lorg/w3c/", "Lorg/xml/", "Lorg/xmlpull/", "Lsun/"
    };

    private final transient DexBackedMethod method;
    private final transient boolean shortMethodSignatures;

    private final int accessFlags;
    private final int annotationCount;
    private final TObjectIntMap<MethodReference> frameworkApiCounts;
    private final TObjectIntMap<FieldReference> frameworkFieldReferenceCounts;
    private final TIntIntMap opCounts;
    private final TObjectIntMap<StringReference> stringReferenceCounts;
    private final int size;

    private int debugItemCount = 0;
    private int instructionCount = 0;
    private int registerCount = 0;
    private int tryCatchCount = 0;
    private int cyclomaticComplexity = -1;
    private double codeEntropy = 0.0D;
    private double codePerplexity = 0.0D;

    DexMethod(DexBackedMethod method, boolean shortMethodSignatures) {
        this.method = method;
        this.shortMethodSignatures = shortMethodSignatures;

        size = method.getSize();
        accessFlags = method.getAccessFlags();
        annotationCount = method.getAnnotations().size();
        frameworkApiCounts = new TObjectIntHashMap<>();
        frameworkFieldReferenceCounts = new TObjectIntHashMap<>();
        opCounts = new TIntIntHashMap();
        stringReferenceCounts = new TObjectIntHashMap<>();
        if (method.getImplementation() != null) {
            analyze(method.getImplementation());
        }
    }

    private void analyze(@Nonnull DexBackedMethodImplementation implementation) {
        debugItemCount = Utils.makeCollection(implementation.getDebugItems()).size();
        registerCount = implementation.getRegisterCount();
        tryCatchCount = implementation.getTryBlocks().size();

        for (Instruction instruction : implementation.getInstructions()) {
            instructionCount++;
            Opcode op = instruction.getOpcode();
            opCounts.adjustOrPutValue(op.apiToValueMap.get(DexFile.TARGET_API), 1, 1);

            if (instruction instanceof ReferenceInstruction) {
                ReferenceInstruction refInstr = (ReferenceInstruction) instruction;
                if (op.referenceType == ReferenceType.METHOD || op.referenceType == ReferenceType.FIELD) {
                    boolean isApiPackage = false;
                    for (String apiPackage : API_PACKAGES) {
                        String refStr = ReferenceUtil.getReferenceString(refInstr.getReference());
                        if (refStr.startsWith(apiPackage)) {
                            isApiPackage = true;
                            break;
                        }
                    }
                    if (!isApiPackage) {
                        continue;
                    }
                }

                switch (op.referenceType) {
                    case ReferenceType.METHOD:
                        MethodReference methodRef = (MethodReference) refInstr.getReference();
                        if (shortMethodSignatures) {
                            ShortMethodReference shortMethodRef = new ShortMethodReference(methodRef);
                            frameworkApiCounts.adjustOrPutValue(shortMethodRef, 1, 1);
                        } else {
                            frameworkApiCounts.adjustOrPutValue(methodRef, 1, 1);
                        }
                        break;
                    case ReferenceType.FIELD:
                        FieldReference fieldRef = (FieldReference) refInstr.getReference();
                        frameworkFieldReferenceCounts.adjustOrPutValue(fieldRef, 1, 1);
                        break;
                    case ReferenceType.STRING:
                        StringReference stringRef = (StringReference) refInstr.getReference();
                        stringReferenceCounts.adjustOrPutValue(stringRef, 1, 1);
                        break;
                }
            }
        }

        analyzeEntropy();
    }

    private void analyzeEntropy() {
        try {
            Field f = DexBackedMethodImplementation.class.getDeclaredField("codeOffset");
            f.setAccessible(true);
            int codeOffset = (Integer) f.get(method.getImplementation());
            DexReaderEntropyCalculator calculator = new DexReaderEntropyCalculator(method.dexFile.readerAt(codeOffset));
            int implementationSize = method.getImplementation().getSize();
            calculator.calculate(codeOffset, implementationSize);
            codeEntropy = calculator.entropy();
            codePerplexity = calculator.perplexity();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public int getAccessFlags() {
        return accessFlags;
    }

    public int getAnnotationCount() {
        return annotationCount;
    }

    public TObjectIntMap<MethodReference> getFrameworkApiCounts() {
        return frameworkApiCounts;
    }

    public int getCyclomaticComplexity() {
        return cyclomaticComplexity;
    }

    public int getDebugItemCount() {
        return debugItemCount;
    }

    public TObjectIntMap<FieldReference> getFrameworkFieldReferenceCounts() {
        return frameworkFieldReferenceCounts;
    }

    public int getInstructionCount() {
        return instructionCount;
    }

    public DexBackedMethod getMethod() {
        return method;
    }

    public int getSize() {
        return size;
    }

    public TIntIntMap getOpCounts() {
        return opCounts;
    }

    public double getCodeEntropy() {
        return codeEntropy;
    }

    public double getCodePerplexity() {
        return codePerplexity;
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
        return ReferenceUtil.getMethodDescriptor(getMethod());
    }

}
