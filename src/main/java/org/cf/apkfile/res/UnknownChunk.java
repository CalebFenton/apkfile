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

import org.pmw.tinylog.Logger;

import javax.annotation.Nullable;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A chunk whose contents are unknown. This is a placeholder until we add a proper chunk for the
 * unknown type.
 */
public final class UnknownChunk extends Chunk {

    private final byte[] header;

    private final byte[] payload;
    private final int dummyChunkSize;

    protected UnknownChunk(ByteBuffer buffer, @Nullable Chunk parent) throws IllegalArgumentException {
        super(buffer, parent);

        // If you're here, something went wrong!
        if (headerSize > chunkSize) {
            Logger.warn("Header size (" + headerSize + ") > chunk size (" + chunkSize + ")");
            header = new byte[0];
            payload = new byte[0];
            dummyChunkSize = Math.min(headerSize + chunkSize, buffer.remaining());
            return;
        }
        if (headerSize + chunkSize > buffer.remaining()) {
            Logger.warn("Chunk size (" + (headerSize + chunkSize) + ") greater than remaining buffer (" + buffer.remaining() + ")");
            header = new byte[0];
            payload = new byte[0];
            dummyChunkSize = buffer.remaining();
            return;
        }
        if (headerSize == 0) {
            Logger.warn("Header size is 0, which is wrong");
            header = new byte[0];
            payload = new byte[0];
            dummyChunkSize = Chunk.METADATA_SIZE;
            return;
        }

        header = new byte[headerSize - Chunk.METADATA_SIZE];
        payload = new byte[chunkSize - headerSize];
        dummyChunkSize = chunkSize;
        buffer.get(header);
        buffer.get(payload);
    }

    @Override
    protected void writeHeader(ByteBuffer output) {
        output.put(header);
    }

    @Override
    protected void writePayload(DataOutput output, ByteBuffer header, boolean shrink) throws IOException {
        output.write(payload);
    }

    @Override
    protected Type getType() {
        return Type.UNKNOWN;
    }

    public int getOriginalChunkSize() {
        return dummyChunkSize;
    }
}
