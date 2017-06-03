package org.cf.apkfile.manifest;

import org.cf.apkfile.res.AttributeId;
import org.cf.apkfile.res.XmlStartElementChunk;

public class SupportsScreens {

    private final boolean resizeable;
    private final boolean smallScreens;
    private final boolean normalScreens;
    private final boolean largeScreens;
    private final boolean xlargeScreens;
    private final int requiresSmallestWidthDp;
    private final boolean anyDensity;
    private final int compatibleWidthLimitDp;
    private final int largestWidthLimitDp;

    SupportsScreens(XmlStartElementChunk supportsScreens) {
        resizeable = supportsScreens.getAttribute(AttributeId.RESIZEABLE, true);
        smallScreens = supportsScreens.getAttribute(AttributeId.SMALL_SCREENS, true);
        normalScreens = supportsScreens.getAttribute(AttributeId.NORMAL_SCREENS, true);

        // Actual default varies across versions, assume true
        largeScreens = supportsScreens.getAttribute(AttributeId.LARGE_SCREENS, true);
        xlargeScreens = supportsScreens.getAttribute(AttributeId.XLARGE_SCREENS, true);

        requiresSmallestWidthDp = supportsScreens.getAttribute(AttributeId.REQUIRES_SMALLEST_WIDTH_DP, -1);
        anyDensity = supportsScreens.getAttribute(AttributeId.ANY_DENSITY, true);
        compatibleWidthLimitDp = supportsScreens.getAttribute(AttributeId.COMPATIBLE_WIDTH_LIMIT_DP, -1);
        largestWidthLimitDp = supportsScreens.getAttribute(AttributeId.LARGEST_WIDTH_LIMIT_DP, -1);
    }

    public int getCompatibleWidthLimitDp() {
        return compatibleWidthLimitDp;
    }

    public int getLargestWidthLimitDp() {
        return largestWidthLimitDp;
    }

    public int getRequiresSmallestWidthDp() {
        return requiresSmallestWidthDp;
    }

    public boolean isAnyDensity() {
        return anyDensity;
    }

    public boolean isLargeScreens() {
        return largeScreens;
    }

    public boolean isNormalScreens() {
        return normalScreens;
    }

    public boolean isResizeable() {
        return resizeable;
    }

    public boolean isSmallScreens() {
        return smallScreens;
    }

    public boolean isXlargeScreens() {
        return xlargeScreens;
    }
}
