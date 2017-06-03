package org.cf.apkfile.manifest;

import org.cf.apkfile.res.AttributeId;
import org.cf.apkfile.res.XmlChunk;
import org.cf.apkfile.res.XmlStartElementChunk;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

public class Provider extends Component {

    private static final String GRANT_URI_PERMISSION = "grant-uri-permission";

    private final String authorities;
    private final Collection<GrantUriPermission> grantUriPermissionList;
    private final boolean grantUriPermissions;
    private final int initOrder;
    private final boolean multiprocess;
    private final String readPermission;
    private final boolean syncable;
    private final String writePermission;

    Provider(XmlStartElementChunk provider, XmlChunk manifest, String packageName) throws XmlPullParserException, IOException {
        grantUriPermissionList = new LinkedList<>();
        authorities = provider.getAttribute(AttributeId.AUTHORITIES);
        grantUriPermissions = provider.getAttribute(AttributeId.GRANT_URI_PERMISSIONS, false);
        initOrder = provider.getAttribute(AttributeId.INIT_ORDER, 0);
        multiprocess = provider.getAttribute(AttributeId.MULTIPROCESS, false);
        readPermission = provider.getAttribute(AttributeId.READ_PERMISSION);
        syncable = provider.getAttribute(AttributeId.SYNCABLE, false);
        writePermission = provider.getAttribute(AttributeId.WRITE_PERMISSION);
        parse(provider, manifest, packageName);
    }

    public final String getAuthorities() {
        return authorities;
    }

    public final Collection<GrantUriPermission> getGrantUriPermissionList() {
        return grantUriPermissionList;
    }

    public final int getInitOrder() {
        return initOrder;
    }

    public final String getReadPermission() {
        return readPermission;
    }

    public final String getWritePermission() {
        return writePermission;
    }

    @Override
    protected void parseUnknown(XmlStartElementChunk chunk, XmlChunk manifest) throws XmlPullParserException, IOException {
        String name = chunk.getName();
        if (GRANT_URI_PERMISSION.equals(name)) {
            grantUriPermissionList.add(new GrantUriPermission(chunk));
        }
    }

    public final boolean isGrantUriPermissions() {
        return grantUriPermissions;
    }

    public final boolean isMultiprocess() {
        return multiprocess;
    }

    public final boolean isSyncable() {
        return syncable;
    }
}
