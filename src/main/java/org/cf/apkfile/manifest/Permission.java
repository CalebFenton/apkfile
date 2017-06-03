package org.cf.apkfile.manifest;

import org.cf.apkfile.res.AttributeId;
import org.cf.apkfile.res.XmlStartElementChunk;

public class Permission {

    private final String description;
    private final String icon;
    private final String label;
    private final String name;
    private final String permissionGroup;
    private final int protectionLevel;

    Permission(XmlStartElementChunk permission) {
        description = permission.getAttribute(AttributeId.DESCRIPTION);
        icon = permission.getAttribute(AttributeId.ICON);
        label = permission.getAttribute(AttributeId.LABEL);
        name = permission.getAttribute(AttributeId.NAME);
        permissionGroup = permission.getAttribute(AttributeId.PERMISSION_GROUP);
        protectionLevel = permission.getAttribute(AttributeId.PROTECTION_LEVEL,0);
    }

    public final String getDescription() {
        return description;
    }

    public final String getIcon() {
        return icon;
    }

    public final String getLabel() {
        return label;
    }

    public final String getName() {
        return name;
    }

    public final String getPermissionGroup() {
        return permissionGroup;
    }

    public final int getProtectionLevel() {
        return protectionLevel;
    }
}
