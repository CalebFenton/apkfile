package org.cf.apkfile.manifest;

import org.cf.apkfile.res.AttributeId;
import org.cf.apkfile.res.XmlStartElementChunk;

public class Metadata {

    private final String name;
    private final String resource;
    private final String value;

    Metadata(XmlStartElementChunk metadata) {
        name = metadata.getAttribute(AttributeId.NAME);
        resource = metadata.getAttribute(AttributeId.RESOURCE);
        value = metadata.getAttribute(AttributeId.VALUE);
    }

    public final String getName() {
        return name;
    }

    public final String getResource() {
        return resource;
    }

    public final String getValue() {
        return value;
    }
}
