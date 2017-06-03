package org.cf.apkfile.manifest;

import org.cf.apkfile.res.AttributeId;
import org.cf.apkfile.res.XmlChunk;
import org.cf.apkfile.res.XmlStartElementChunk;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class Service extends Component {

    private final boolean isolatedProcess;

    Service(XmlStartElementChunk service, XmlChunk manifest, String packageName) throws XmlPullParserException, IOException {
        isolatedProcess = service.getAttribute(AttributeId.ISOLATED_PROCESS, false);
        parse(service, manifest, packageName);
    }

    public boolean isIsolatedProcess() {
        return isolatedProcess;
    }
}
