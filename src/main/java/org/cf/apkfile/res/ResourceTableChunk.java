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

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a resource table structure. Its sub-chunks contain:
 * <p>
 * <ul>
 * <li>A {@link StringPoolChunk} containing all string values in the entire resource table. It does
 * not, however, contain the names of entries or type identifiers.
 * <li>One or more {@link PackageChunk}.
 * </ul>
 */
public final class ResourceTableChunk extends ChunkWithChunks {

    /**
     * The packages contained in this resource table.
     */
    private final Map<String, PackageChunk> packages = new HashMap<>();

    /**
     * A string pool containing all string resource values in the entire resource table.
     */
    private StringPoolChunk stringPool;

    protected ResourceTableChunk(ByteBuffer buffer, @Nullable Chunk parent) {
        super(buffer, parent);
        // packageCount. We ignore this, because we already know how many chunks we have.
        Preconditions.checkState(buffer.getInt() >= 1, "ResourceTableChunk package count was < 1.");
    }

    @Override
    protected void init(ByteBuffer buffer) {
        super.init(buffer);
        packages.clear();
        for (Chunk chunk : getChunks().values()) {
            if (chunk instanceof PackageChunk) {
                PackageChunk packageChunk = (PackageChunk) chunk;
                packages.put(packageChunk.getPackageName(), packageChunk);
            } else if (chunk instanceof StringPoolChunk) {
                stringPool = (StringPoolChunk) chunk;
            }
        }
        Preconditions.checkNotNull(stringPool, "ResourceTableChunk must have a string pool.");
    }

    /**
     * Returns the string pool containing all string resource values in the resource table.
     */
    public StringPoolChunk getStringPool() {
        return stringPool;
    }

    /**
     * Returns the package with the given {@code packageName}. Else, returns null.
     */
    @Nullable
    public PackageChunk getPackage(String packageName) {
        return packages.get(packageName);
    }

    @Nullable
    public PackageChunk getPackage(int packageId) {
        for (PackageChunk packageChunk : packages.values()) {
            if (packageChunk.getId() == packageId) {
                return packageChunk;
            }
        }
        return null;
    }

    /**
     * Returns the packages contained in this resource table.
     */
    public Collection<PackageChunk> getPackages() {
        return Collections.unmodifiableCollection(packages.values());
    }

    @Override
    protected Type getType() {
        return Chunk.Type.TABLE;
    }

    private static int getEntryIndex(int id) {
        return id & 0xffff;
    }

    public int getPackageCount() {
        return packages.size();
    }

    private static int getPackageId(int entryId) {
        return (entryId >> 24) & 0xff;
    }

    private static int getTypeId(int entryId) {
        return (entryId >> 16) & 0xff;
    }

    private Collection<TypeChunk> getResourceTypes(int entryId) {
        int packageId = getPackageId(entryId);
        PackageChunk packageChunk = getPackage(packageId);
        if (packageChunk == null) {
            return null;
        }
        int typeId = getTypeId(entryId);

        return packageChunk.getTypeChunks(typeId);
    }

    @Nullable
    String resolve(int id) {
        int entryIndex = getEntryIndex(id);
        Collection<TypeChunk> typeChunks = getResourceTypes(id);
        if (typeChunks == null) {
            return null;
        }

        for (TypeChunk typeChunk : typeChunks) {
            TypeChunk.Entry entry = typeChunk.getEntries().get(entryIndex);
            if (entry != null) {
                if (entry.value() == null) {
                    // Must be a complex
                    int packageId = getPackageId(id);
                    PackageChunk packageChunk = getPackage(packageId);
                    String valueType;
                    if (packageChunk == null) {
                        // No idea why this happens
                        valueType = "unknown";
                    } else {
                        int typeIndex = getTypeId(id) -1;
                        valueType = packageChunk.getTypeStringPool().getString(typeIndex);
                    }

                    return "@" + valueType + "/" + entry.key();
                }

                return entry.value().getString(getStringPool(), this);
            }
        }

        return null;
    }

    @Override
    protected void writeHeader(ByteBuffer output) {
        super.writeHeader(output);
        output.putInt(packages.size());
    }
}
