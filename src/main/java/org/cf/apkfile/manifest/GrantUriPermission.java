package org.cf.apkfile.manifest;

import org.cf.apkfile.res.AttributeId;
import org.cf.apkfile.res.XmlStartElementChunk;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class GrantUriPermission {

    private final String path;
    private final String pathPattern;
    private final String pathPrefix;

    GrantUriPermission(XmlStartElementChunk grantUriPermission) throws XmlPullParserException, IOException {
        path = grantUriPermission.getAttribute(AttributeId.PATH);
        pathPattern = grantUriPermission.getAttribute(AttributeId.PATH_PATTERN);
        pathPrefix = grantUriPermission.getAttribute(AttributeId.PATH_PREFIX);
    }

    public final String getPath() {
        return path;
    }

    public final String getPathPattern() {
        return pathPattern;
    }

    public final String getPathPrefix() {
        return pathPrefix;
    }
}
