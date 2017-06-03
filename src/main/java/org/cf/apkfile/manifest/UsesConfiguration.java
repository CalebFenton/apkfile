package org.cf.apkfile.manifest;

import org.cf.apkfile.res.AttributeId;
import org.cf.apkfile.res.XmlStartElementChunk;

public class UsesConfiguration {

    private final boolean reqFiveWayNav;
    private final boolean reqHardKeyboard;
    private final int reqKeyboardType;
    private final int reqNavigation;
    private final int reqTouchScreen;

    UsesConfiguration(XmlStartElementChunk usesConfiguration) {
        reqFiveWayNav = usesConfiguration.getAttribute(AttributeId.REQ_FIVE_WAY_NAV, false);
        reqHardKeyboard = usesConfiguration.getAttribute(AttributeId.REQ_HARD_KEYBOARD, false);
        reqKeyboardType = usesConfiguration.getAttribute(AttributeId.REQ_KEYBOARD_TYPE, 0);
        reqNavigation = usesConfiguration.getAttribute(AttributeId.REQ_NAVIGATION, 0);
        reqTouchScreen = usesConfiguration.getAttribute(AttributeId.REQ_TOUCH_SCREEN, 0);
    }

    public final boolean getReqFiveWayNav() {
        return reqFiveWayNav;
    }

    public final boolean getReqHardKeyboard() {
        return reqHardKeyboard;
    }

    public final int getReqKeyboardType() {
        return reqKeyboardType;
    }

    public final int getReqNavigation() {
        return reqNavigation;
    }

    public final int getReqTouchScreen() {
        return reqTouchScreen;
    }
}
