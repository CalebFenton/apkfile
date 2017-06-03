package org.cf.apkfile.manifest;

import org.cf.apkfile.res.AttributeId;
import org.cf.apkfile.res.XmlStartElementChunk;

public class UsesFeature {

    private final int glEsVersion;
    private final String name;
    private final boolean required;

    UsesFeature(XmlStartElementChunk usesFeature) {
        glEsVersion = usesFeature.getAttribute(AttributeId.GL_ES_VERSION, 0);
        name = usesFeature.getAttribute(AttributeId.NAME);
        required = usesFeature.getAttribute(AttributeId.REQUIRED, true);
    }

    public final int getGlEsVersion() {
        return glEsVersion;
    }

    public final String getName() {
        return name;
    }

    public final boolean getRequired() {
        return required;
    }
}
