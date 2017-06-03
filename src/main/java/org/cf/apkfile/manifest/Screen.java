package org.cf.apkfile.manifest;

import org.cf.apkfile.res.AttributeId;
import org.cf.apkfile.res.XmlStartElementChunk;

public class Screen {

    private final int screenDensity;
    private final int screenSize;

    Screen(XmlStartElementChunk screen) {
        screenDensity = screen.getAttribute(AttributeId.SCREEN_DENSITY, 0);
        screenSize = screen.getAttribute(AttributeId.SCREEN_SIZE, 0);
    }

    public final int getScreenDensity() {
        return screenDensity;
    }

    public final int getScreenSize() {
        return screenSize;
    }
}
