package org.cf.apkfile.manifest;

import org.cf.apkfile.res.AttributeId;
import org.cf.apkfile.res.XmlStartElementChunk;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class Data {

    private final String host;
    private final String mimeType;
    private final String path;
    private final String pathPattern;
    private final String pathPrefix;
    private final String port;
    private final String scheme;

    Data(XmlStartElementChunk dataChunk) throws XmlPullParserException, IOException {
        host = dataChunk.getAttribute(AttributeId.HOST);
        mimeType = dataChunk.getAttribute(AttributeId.MIME_TYPE);
        path = dataChunk.getAttribute(AttributeId.PATH);
        pathPattern = dataChunk.getAttribute(AttributeId.PATH_PATTERN);
        pathPrefix = dataChunk.getAttribute(AttributeId.PATH_PREFIX);
        port = dataChunk.getAttribute(AttributeId.PORT);
        scheme = dataChunk.getAttribute(AttributeId.SCHEME);
    }

    public final String getHost() {
        return host;
    }

    public final String getMimeType() {
        return mimeType;
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

    public final String getPort() {
        return port;
    }

    public final String getScheme() {
        return scheme;
    }
}
