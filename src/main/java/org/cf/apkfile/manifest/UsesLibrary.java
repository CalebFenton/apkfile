package org.cf.apkfile.manifest;

import org.cf.apkfile.res.AttributeId;
import org.cf.apkfile.res.XmlStartElementChunk;

public class UsesLibrary {

    private final String name;
    private final boolean required;

    UsesLibrary(XmlStartElementChunk usesLibrary) {
        name = usesLibrary.getAttribute(AttributeId.NAME);
        required = usesLibrary.getAttribute(AttributeId.REQUIRED, true);
    }

    public final String getName() {
        return name;
    }

    public final boolean getRequired() {
        return required;
    }
}
