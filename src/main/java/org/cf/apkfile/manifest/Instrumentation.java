package org.cf.apkfile.manifest;

import org.cf.apkfile.res.AttributeId;
import org.cf.apkfile.res.XmlStartElementChunk;

public class Instrumentation {

    private final boolean functionalTest;
    private final boolean handleProfiling;
    private final String icon;
    private final String label;
    private final String name;
    private final String targetPackage;

    Instrumentation(XmlStartElementChunk instrumentation) {
        functionalTest = instrumentation
                .getAttribute(AttributeId.FUNCTIONAL_TEST, false);
        handleProfiling = instrumentation
                .getAttribute(AttributeId.HANDLE_PROFILING, false);
        icon = instrumentation.getAttribute(AttributeId.ICON);
        label = instrumentation.getAttribute(AttributeId.LABEL);
        name = instrumentation.getAttribute(AttributeId.NAME);
        targetPackage = instrumentation.getAttribute(AttributeId.TARGET_PACKAGE);
    }

    public final String getIcon() {
        return icon;
    }

    public final String getLabel() {
        return label;
    }

    public final String getName() {
        return name;
    }

    public final String getTargetPackage() {
        return targetPackage;
    }

    public final boolean isFunctionalTest() {
        return functionalTest;
    }

    public final boolean isHandleProfiling() {
        return handleProfiling;
    }
}
