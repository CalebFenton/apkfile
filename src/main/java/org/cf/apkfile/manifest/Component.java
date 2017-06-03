package org.cf.apkfile.manifest;

import org.cf.apkfile.res.AttributeId;
import org.cf.apkfile.res.Chunk;
import org.cf.apkfile.res.XmlChunk;
import org.cf.apkfile.res.XmlStartElementChunk;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract class Component {

    private static final String INTENT_FILTER = "intent-filter";
    private static final String META_DATA = "meta-data";

    private final Collection<IntentFilter> intentFilters;
    private final Collection<Metadata> metaData;
    private boolean directBootAware;
    private boolean enabled;
    private boolean exported;
    private String icon;
    private String label;
    private String name;
    private String permission;
    private String process;

    Component() throws XmlPullParserException, IOException {
        intentFilters = new LinkedList<>();
        metaData = new LinkedList<>();
    }

    public final String getIcon() {
        return icon;
    }

    public final Collection<IntentFilter> getIntentFilters() {
        return intentFilters;
    }

    public final String getLabel() {
        return label;
    }

    public final Collection<Metadata> getMetaData() {
        return metaData;
    }

    /**
     * @return full component name, including the package name
     */
    public final String getName() {
        return name;
    }

    public final String getPermission() {
        return permission;
    }

    public final boolean isEnabled() {
        return enabled;
    }

    public final boolean isExported() {
        return exported;
    }

    protected void parse(XmlStartElementChunk component, XmlChunk manifest, String packageName) throws XmlPullParserException, IOException {
        directBootAware = component.getAttribute(AttributeId.DIRECT_BOOT_AWARE, false);
        String componentName = component.getAttribute(AttributeId.NAME);
        name = Utils.ensureFullName(componentName, packageName);
        permission = component.getAttribute(AttributeId.PERMISSION);
        label = component.getAttribute(AttributeId.LABEL);
        icon = component.getAttribute(AttributeId.ICON);
        exported = component.getAttribute(AttributeId.EXPORTED, false);
        enabled = component.getAttribute(AttributeId.ENABLED, true);
        process = component.getAttribute(AttributeId.PROCESS);

        List<Chunk> componentChunks = manifest.getChunksWithin(component);
        for (Chunk c : componentChunks) {
            if (!(c instanceof XmlStartElementChunk)) {
                continue;
            }

            XmlStartElementChunk chunk = (XmlStartElementChunk) c;
            String name = chunk.getName();
            if (INTENT_FILTER.equals(name)) {
                intentFilters.add(new IntentFilter(chunk, manifest));
            } else if (META_DATA.equals(name)) {
                metaData.add(new Metadata(chunk));
            } else {
                parseUnknown(chunk, manifest);
            }
        }
    }

    protected void parseUnknown(XmlStartElementChunk chunk, XmlChunk manifest) throws XmlPullParserException, IOException {

    }

    public final String getProcess() {
        return process;
    }

    public final boolean isDirectBootAware() {
        return directBootAware;
    }
}
