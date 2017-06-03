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

public class IntentFilter {

    private static final String ACTION = "action";
    private static final String CATEGORY = "category";
    private static final String DATA = "data";

    private final List<String> actions;
    private final List<String> categories;
    private final List<Data> data;
    private final String icon;
    private final String label;
    private final int priority;

    IntentFilter(XmlStartElementChunk intent, XmlChunk manifest) throws XmlPullParserException, IOException {
        actions = new LinkedList<>();
        categories = new LinkedList<>();
        data = new LinkedList<>();
        icon = intent.getAttribute(AttributeId.ICON);
        label = intent.getAttribute(AttributeId.LABEL);
        priority = intent.getAttribute(AttributeId.PRIORITY,0);

        List<Chunk> componentChunks = manifest.getChunksWithin(intent);
        for (Chunk c : componentChunks) {
            if (!(c instanceof XmlStartElementChunk)) {
                continue;
            }

            XmlStartElementChunk chunk = (XmlStartElementChunk) c;
            String name = chunk.getName();
            if (ACTION.equals(name)) {
                actions.add(chunk.getAttribute(AttributeId.NAME));
            } else if (CATEGORY.equals(name)) {
                categories.add(chunk.getAttribute(AttributeId.NAME));
            } else if (DATA.equals(name)) {
                data.add(new Data(chunk));
            }
        }
    }

    public final Collection<String> getActions() {
        return actions;
    }

    public final Collection<String> getCategories() {
        return categories;
    }

    public final Collection<Data> getData() {
        return data;
    }

    public final String getIcon() {
        return icon;
    }

    public final String getLabel() {
        return label;
    }

    public final int getPriority() {
        return priority;
    }
}
