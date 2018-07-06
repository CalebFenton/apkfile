package org.cf.apkfile.analysis;

import com.google.common.collect.ImmutableSet;
import org.cf.apkfile.dex.DexFile;
import org.cf.apkfile.dex.DexMethod;
import org.jf.dexlib2.dexbacked.DexBackedMethod;
import org.jf.dexlib2.dexbacked.DexBackedMethodImplementation;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.iface.instruction.ReferenceInstruction;
import org.jf.dexlib2.iface.instruction.formats.PackedSwitchPayload;
import org.jf.dexlib2.iface.instruction.formats.SparseSwitchPayload;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.util.ReferenceUtil;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ComplexityAnalyzer {

    private final Collection<DexFile> dexFiles;

    public ComplexityAnalyzer(DexFile dexFile) {
        this(ImmutableSet.of(dexFile));
    }

    public ComplexityAnalyzer(Collection<DexFile> dexFiles) {
        this.dexFiles = dexFiles;
    }

    public void analyze() {
        for (DexFile dexFile : dexFiles) {
            for (DexMethod dexMethod : dexFile.getMethodDescriptorToMethod().values()) {
                if (dexMethod.getCyclomaticComplexity() >= 0) {
                    continue;
                }

                DexBackedMethod method = dexMethod.getMethod();
                DexBackedMethodImplementation implementation = method.getImplementation();
                if (implementation != null) {
                    int complexity = calculateComplexity(implementation, dexFiles, new HashSet<>());
                    dexMethod.setCyclomaticComplexity(complexity);
                }
            }

        }

    }

    private int calculateComplexity(@Nonnull DexBackedMethodImplementation implementation,
                                    Collection<DexFile> dexFiles,
                                    Set<String> visitedMethodDescriptors) {
        int decisionPoints = 0;
        int exits = 0;
        for (Instruction instruction : implementation.getInstructions()) {
            switch (instruction.getOpcode()) {
                case IF_EQ:
                case IF_EQZ:
                case IF_GE:
                case IF_GEZ:
                case IF_GT:
                case IF_GTZ:
                case IF_LE:
                case IF_LEZ:
                case IF_LT:
                case IF_LTZ:
                case IF_NE:
                case IF_NEZ:
                    decisionPoints += 1;
                    break;
                case PACKED_SWITCH_PAYLOAD:
                    decisionPoints += ((PackedSwitchPayload) instruction).getSwitchElements()
                            .size();
                    break;
                case RETURN:
                case RETURN_OBJECT:
                case RETURN_VOID:
                case RETURN_VOID_BARRIER:
                case RETURN_VOID_NO_BARRIER:
                case RETURN_WIDE:
                    exits += 1;
                    break;
                case SPARSE_SWITCH_PAYLOAD:
                    decisionPoints += ((SparseSwitchPayload) instruction).getSwitchElements()
                            .size();
                    break;
                case THROW:
                case THROW_VERIFICATION_ERROR:
                    // TODO: validate this makes sense...
                    // for every throw, there must be a try, which is kind of a branch
                    // otherwise it's possible to have 0 branches and 1000 exits and have negative complexity
                    decisionPoints += 1;
                    exits += 1;
                    break;
                case INVOKE_DIRECT:
                case INVOKE_DIRECT_EMPTY:
                case INVOKE_DIRECT_RANGE:
                case INVOKE_INTERFACE:
                case INVOKE_INTERFACE_RANGE:
                case INVOKE_OBJECT_INIT_RANGE:
                case INVOKE_POLYMORPHIC:
                case INVOKE_POLYMORPHIC_RANGE:
                case INVOKE_STATIC:
                case INVOKE_STATIC_RANGE:
                case INVOKE_SUPER:
                case INVOKE_SUPER_QUICK:
                case INVOKE_SUPER_RANGE:
                case INVOKE_SUPER_QUICK_RANGE:
                case INVOKE_VIRTUAL:
                case INVOKE_VIRTUAL_QUICK:
                case INVOKE_VIRTUAL_RANGE:
                case INVOKE_VIRTUAL_QUICK_RANGE:
                    ReferenceInstruction refInstr = (ReferenceInstruction) instruction;
                    MethodReference methodRef = (MethodReference) refInstr.getReference();
                    String methodDescriptor = ReferenceUtil.getMethodDescriptor(methodRef);
                    if (visitedMethodDescriptors.contains(methodDescriptor)) {
                        decisionPoints += 1;
                        break;
                    }

                    String definingClass = methodRef.getDefiningClass();
                    boolean isLocal = false;
                    for (DexFile dexFile : dexFiles) {
                        if (dexFile.isLocalClass(definingClass)) {
                            isLocal = true;
                            break;
                        }
                    }
                    if (!isLocal) {
                        // Don't know the complexity of non-local classes...
                        decisionPoints += 1;
                        break;
                    }

                    for (DexFile dexFile : dexFiles) {
                        DexMethod dexMethod = dexFile.getMethod(methodDescriptor);
                        if (dexMethod == null) {
                            continue;
                        }

                        if (dexMethod.getCyclomaticComplexity() >= 0) {
                            decisionPoints += dexMethod.getCyclomaticComplexity();
                        } else {
                            DexBackedMethod method = dexMethod.getMethod();
                            DexBackedMethodImplementation calledImplementation = method.getImplementation();
                            if (calledImplementation != null) {
                                visitedMethodDescriptors.add(methodDescriptor);
                                int complexity = calculateComplexity(calledImplementation, dexFiles, visitedMethodDescriptors);
                                dexMethod.setCyclomaticComplexity(complexity);
                                decisionPoints += complexity;
                                break;
                            }
                        }
                    }
                    break;
            }
        }

        return decisionPoints - exits + 2;
    }
}
