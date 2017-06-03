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

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents an XML chunk structure.
 * <p>
 * <p>An XML chunk can contain many nodes as well as a string pool which contains all of the strings
 * referenced by the nodes.
 */
public final class XmlChunk extends ChunkWithChunks {

    protected XmlChunk(ByteBuffer buffer, @Nullable Chunk parent) {
        super(buffer, parent);
    }

    @Override
    protected Type getType() {
        return Chunk.Type.XML;
    }

    /**
     * Returns a string at the provided (0-based) index if the index exists in the string pool.
     */
    public String getString(int index) {
        for (Chunk chunk : getChunks().values()) {
            if (chunk instanceof StringPoolChunk) {
                return ((StringPoolChunk) chunk).getString(index);
            }
        }
        throw new IllegalStateException("XmlChunk did not contain a string pool.");
    }

    @Nullable
    Integer getResourceIndex(AttributeId attributeId) {
        for (Chunk chunk : getChunks().values()) {
            if (chunk instanceof XmlResourceMapChunk) {
                XmlResourceMapChunk resourceMap = (XmlResourceMapChunk) chunk;
                return resourceMap.getResourceIndex(attributeId);
            }
        }
        throw new IllegalStateException("XmlChunk did not contain a resource map.");
    }

    public List<Chunk> getChunksWithin(XmlStartElementChunk startChunk) {
        String startName = startChunk.getName();
        List<Chunk> chunksWithin = new LinkedList<>();
        int depth = 0;
        for (Chunk chunk : getChunks().values()) {
            if (depth == 0) {
                if (chunk == startChunk) {
                    depth++;
                }
            } else {
                chunksWithin.add(chunk);
                if (chunk instanceof XmlStartElementChunk) {
                    String name = ((XmlStartElementChunk) chunk).getName();
                    if (startName.equals(name)) {
                        depth++;
                    }
                } else if (chunk instanceof XmlEndElementChunk) {
                    String name = ((XmlEndElementChunk) chunk).getName();
                    if (startName.equals(name)) {
                        depth--;
                    }
                    if (depth == 0) {
                        chunksWithin.remove(chunksWithin.size() - 1);
                        return chunksWithin;
                    }
                }
            }
        }

        return chunksWithin;
    }
}
