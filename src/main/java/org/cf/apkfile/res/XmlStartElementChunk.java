/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cf.apkfile.res;

import com.google.common.base.Preconditions;
import org.cf.apkfile.dex.DexClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Represents the beginning of an XML node.
 */
public final class XmlStartElementChunk extends XmlNodeChunk {

    private static final transient Logger logger = LoggerFactory.getLogger(XmlStartElementChunk.class);

    /**
     * A string reference to the namespace URI, or -1 if not present.
     */
    private final int namespace;

    /**
     * A string reference to the element name that this chunk represents.
     */
    private final int name;

    /**
     * The offset to the start of the attributes payload.
     */
    private final int attributeStart;

    /**
     * The number of attributes in the original buffer.
     */
    private final int attributeCount;

    /**
     * The (0-based) index of the id attribute, or -1 if not present.
     */
    private final int idIndex;

    /**
     * The (0-based) index of the class attribute, or -1 if not present.
     */
    private final int classIndex;

    /**
     * The (0-based) index of the style attribute, or -1 if not present.
     */
    private final int styleIndex;

    private final Map<Integer, XmlAttribute> nameIndexToAttribute = new HashMap<>();

    protected XmlStartElementChunk(ByteBuffer buffer, @Nullable Chunk parent) {
        super(buffer, parent);
        namespace = buffer.getInt();
        name = buffer.getInt();
        attributeStart = (buffer.getShort() & 0xFFFF);
        int attributeSize = (buffer.getShort() & 0xFFFF);
        Preconditions
                .checkState(attributeSize == XmlAttribute.SIZE, "attributeSize is wrong size. Got %s, want %s", attributeSize, XmlAttribute.SIZE);
        attributeCount = (buffer.getShort() & 0xFFFF);

        // The following indices are 1-based and need to be adjusted.
        idIndex = (buffer.getShort() & 0xFFFF) - 1;
        classIndex = (buffer.getShort() & 0xFFFF) - 1;
        styleIndex = (buffer.getShort() & 0xFFFF) - 1;
    }

    @Override
    protected void init(ByteBuffer buffer) {
        super.init(buffer);
        parseAttributes(buffer);
    }

    private void parseAttributes(ByteBuffer buffer) {
        int offset = this.offset + getHeaderSize() + attributeStart;
        int endOffset = offset + XmlAttribute.SIZE * attributeCount;
        buffer.mark();
        buffer.position(offset);
        while (offset < endOffset) {
            XmlAttribute attribute = XmlAttribute.create(buffer, this);
            nameIndexToAttribute.put(attribute.nameIndex(), attribute);
            offset += XmlAttribute.SIZE;
        }
        buffer.reset();
    }

    private List<XmlAttribute> enumerateAttributes(ByteBuffer buffer) {
        List<XmlAttribute> result = new ArrayList<>(attributeCount);
        int offset = this.offset + getHeaderSize() + attributeStart;
        int endOffset = offset + XmlAttribute.SIZE * attributeCount;
        buffer.mark();
        buffer.position(offset);

        while (offset < endOffset) {
            result.add(XmlAttribute.create(buffer, this));
            offset += XmlAttribute.SIZE;
        }

        buffer.reset();
        return result;
    }

    /**
     * Returns the namespace URI, or the empty string if not present.
     */
    public String getNamespace() {
        return getString(namespace);
    }

    /**
     * Returns the element name that this chunk represents.
     */
    public String getName() {
        return getString(name);
    }

    /**
     * Returns an unmodifiable collection of this XML element's attributes.
     */
    public Collection<XmlAttribute> getAttributes() {
        return Collections.unmodifiableCollection(nameIndexToAttribute.values());
    }

    @Override
    protected Type getType() {
        return Chunk.Type.XML_START_ELEMENT;
    }

    @Override
    protected void writePayload(DataOutput output, ByteBuffer header, boolean shrink) throws IOException {
        super.writePayload(output, header, shrink);
        output.writeInt(namespace);
        output.writeInt(name);
        output.writeShort((short) XmlAttribute.SIZE);  // attribute start
        output.writeShort((short) XmlAttribute.SIZE);
        output.writeShort((short) getAttributes().size());
        output.writeShort((short) (idIndex + 1));
        output.writeShort((short) (classIndex + 1));
        output.writeShort((short) (styleIndex + 1));
        for (XmlAttribute attribute : getAttributes()) {
            output.write(attribute.toByteArray(shrink));
        }
    }

    @Nullable
    public String getAttribute(String name) {
        for (XmlAttribute attribute : getAttributes()) {
            if (name.equals(attribute.name())) {
                ResourceTableChunk resourceTable = getResourceTable();
                return attribute.typedValue().getString(getStringPool(), resourceTable);
            }
        }

        return "";
    }

    @Nullable
    private Integer getResourceIndex(AttributeId attributeId) {
        Chunk parent = getParent();
        while (parent != null) {
            if (parent instanceof XmlChunk) {
                return ((XmlChunk) parent).getResourceIndex(attributeId);
            }
            parent = parent.getParent();
        }
        throw new IllegalStateException("XmlNodeChunk did not have an XmlChunk parent.");
    }

    public String getAttribute(AttributeId attributeId) {
        Integer resourceIndex = getResourceIndex(attributeId);
        if (resourceIndex == null) {
            return "";
        }

        XmlAttribute attribute = nameIndexToAttribute.get(resourceIndex);
        if (attribute != null) {
            ResourceTableChunk resourceTable = getResourceTable();
            return attribute.typedValue().getString(getStringPool(), resourceTable);
        }

        return "";
    }

    public boolean getAttribute(AttributeId attributeId, boolean defaultValue) {
        String value = getAttribute(attributeId);

        return convertValueToBoolean(value, defaultValue);
    }

    public int getAttribute(AttributeId attributeId, int defaultValue) {
        String value = getAttribute(attributeId);

        return convertValueToInteger(value, defaultValue);
    }

    private int convertValueToInteger(String value, int defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        } else {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                logger.warn("Problem converting value to integer", e);
                return defaultValue;
            }
        }
    }

    private boolean convertValueToBoolean(String value, boolean defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        } else {
            return value.equals(Boolean.TRUE.toString());
        }
    }

    public int getAttribute(String name, int defaultValue) {
        String value = getAttribute(name);

        return convertValueToInteger(value, defaultValue);
    }

    public boolean getAttribute(String name, boolean defaultValue) {
        String value = getAttribute(name);

        return convertValueToBoolean(value, defaultValue);
    }

    /**
     * Returns a brief description of this XML node. The representation of this information is
     * subject to change, but below is a typical example:
     * <p>
     * <pre>
     * "XmlStartElementChunk{line=1234, comment=My awesome comment., namespace=foo, name=bar, ...}"
     * </pre>
     */
    @Override
    public String toString() {
        return String.format("XmlStartElementChunk{line=%d, comment=%s, namespace=%s, name=%s, attributes=%s}", getLineNumber(), getComment(),
                getNamespace(), getName(), getAttributes().toString());
    }
}
