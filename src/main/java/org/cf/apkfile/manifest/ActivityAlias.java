package org.cf.apkfile.manifest;

import org.cf.apkfile.res.AttributeId;
import org.cf.apkfile.res.XmlChunk;
import org.cf.apkfile.res.XmlStartElementChunk;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;


public class ActivityAlias extends Component {

    private final String targetActivity;

    ActivityAlias(XmlStartElementChunk activityAlias, XmlChunk manifest, String packageName) throws XmlPullParserException, IOException {
        targetActivity = activityAlias.getAttribute(AttributeId.TARGET_ACTIVITY);
        parse(activityAlias, manifest, packageName);
    }

    public final String getTargetActivity() {
        return targetActivity;
    }
}
