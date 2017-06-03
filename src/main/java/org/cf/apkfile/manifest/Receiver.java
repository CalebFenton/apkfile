package org.cf.apkfile.manifest;

import org.cf.apkfile.res.AttributeId;
import org.cf.apkfile.res.XmlChunk;
import org.cf.apkfile.res.XmlStartElementChunk;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class Receiver extends Component {

    Receiver(XmlStartElementChunk receiver, XmlChunk manifest, String packageName) throws XmlPullParserException, IOException {
        parse(receiver, manifest, packageName);
    }
}
