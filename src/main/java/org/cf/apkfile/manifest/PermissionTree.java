package org.cf.apkfile.manifest;

import org.cf.apkfile.res.AttributeId;
import org.cf.apkfile.res.XmlStartElementChunk;

public class PermissionTree {

    private final String icon;
    private final String label;
    private final String name;

    PermissionTree(XmlStartElementChunk permissionTree) {
        icon = permissionTree.getAttribute(AttributeId.ICON);
        label = permissionTree.getAttribute(AttributeId.LABEL);
        name = permissionTree.getAttribute(AttributeId.NAME);
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
