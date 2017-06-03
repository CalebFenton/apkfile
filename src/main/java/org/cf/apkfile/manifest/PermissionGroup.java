package org.cf.apkfile.manifest;

import org.cf.apkfile.res.AttributeId;
import org.cf.apkfile.res.XmlStartElementChunk;

public class PermissionGroup {

    private final String description;
    private final String icon;
    private final String label;
    private final String name;

    PermissionGroup(XmlStartElementChunk permissionGroup) {
        description = permissionGroup.getAttribute(AttributeId.DESCRIPTION);
        icon = permissionGroup.getAttribute(AttributeId.ICON);
        label = permissionGroup.getAttribute(AttributeId.LABEL);
        name = permissionGroup.getAttribute(AttributeId.NAME);
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
}
