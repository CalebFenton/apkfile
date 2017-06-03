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

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.primitives.UnsignedBytes;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;

/**
 * Represents a single typed resource value.
 */
@AutoValue
public abstract class ResourceValue implements SerializableResource {

    private static final int COMPLEX_RADIX_SHIFT = 4;
    private static final int COMPLEX_RADIX_MASK = 0x3;
    private static final int COMPLEX_UNIT_MASK = 0xf;
    private static final int COMPLEX_MANTISSA_SHIFT = 8;
    private static final int COMPLEX_MANTISSA_MASK = 0xffffff;
    private static final float MANTISSA_MULT = 1.0f / (1 << COMPLEX_MANTISSA_SHIFT);
    private static final float[] RADIX_MULTS = new float[]{
            1.0f * MANTISSA_MULT,
            1.0f / (1 << 7) * MANTISSA_MULT,
            1.0f / (1 << 15) * MANTISSA_MULT,
            1.0f / (1 << 23) * MANTISSA_MULT
    };

    private static final String[] DIMENSION_UNIT_STRS = new String[]{"px",
            "dip", "sp", "pt", "in", "mm"};
    private static final String[] FRACTION_UNIT_STRS = new String[]{"%", "%p"};

    /**
     * The serialized size in bytes of a {@link ResourceValue}.
     */
    public static final int SIZE = 8;

    public static ResourceValue create(ByteBuffer buffer) {
        int size = (buffer.getShort() & 0xFFFF);
        buffer.get();  // Unused
        Type type = Type.fromCode(buffer.get());
        int data = buffer.getInt();
        return new AutoValue_ResourceValue(size, type, data);
    }

    /**
     * The length in bytes of this value.
     */
    public abstract int size();

    /**
     * The raw data type of this value.
     */
    public abstract Type type();

    /**
     * The actual 4-byte value; interpretation of the value depends on {@code dataType}.
     */
    public abstract int data();

    @Override
    public byte[] toByteArray() {
        return toByteArray(false);
    }

    @Override
    public byte[] toByteArray(boolean shrink) {
        ByteBuffer buffer = ByteBuffer.allocate(SIZE).order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort((short) size());
        buffer.put((byte) 0);  // Unused
        buffer.put(type().code());
        buffer.putInt(data());
        return buffer.array();
    }

    private static float complexToFloat(int complex) {
        return (complex & (COMPLEX_MANTISSA_MASK << COMPLEX_MANTISSA_SHIFT)) * RADIX_MULTS[(complex >> COMPLEX_RADIX_SHIFT) & COMPLEX_RADIX_MASK];
    }

    protected String getString(StringPoolChunk stringPool, @Nullable ResourceTableChunk resourceTable) {
        switch (type()) {
            case INT_BOOLEAN:
                return data() != 0 ? Boolean.TRUE.toString() : Boolean.FALSE.toString();
            case REFERENCE:
                if (resourceTable != null) {
                    String value = resourceTable.resolve(data());
                    if (value != null) {
                        return value;
                    }
                }
                return "U[" + data() + "]";
            case STRING:
                return stringPool.getString(data());
            case INT_HEX:
                // Prefer numeric values when possible. No reason to care about hex formatting.
                // return String.format("0x%08X", data());
            case INT_DEC:
                return Integer.toString(data());
            case NULL:
                return "";
            case ATTRIBUTE:
                return "A[" + data() + "]";
            case FLOAT:
                return Float.toString(Float.intBitsToFloat(data()));
            case DIMENSION:
                return Float.toString(complexToFloat(data())) + DIMENSION_UNIT_STRS[(data()) & COMPLEX_UNIT_MASK];
            case FRACTION:
                return Float.toString(complexToFloat(data())) + DIMENSION_UNIT_STRS[(data()) & COMPLEX_UNIT_MASK];
        }

        if (type().code >= Type.INT_COLOR_ARGB8.code && type().code <= Type.INT_COLOR_RGB4.code) {
            String res = String.format("%08x", data());
            char[] vals = res.toCharArray();
            switch (type()) {
                default:
                case INT_COLOR_ARGB8:
                    // #AaRrGgBb
                    break;
                case INT_COLOR_RGB8:
                    // #FFRrGgBb->#RrGgBb
                    res = res.substring(2);
                    break;
                case INT_COLOR_ARGB4:
                    // #AARRGGBB->#ARGB
                    res = String.valueOf(vals[0]) + vals[2] + vals[4] + vals[6];
                    break;
                case INT_COLOR_RGB4:
                    // #FFRRGGBB->#RGB
                    res = String.valueOf(vals[2]) + vals[4] + vals[6];
                    break;
            }
            return "#" + res;
        } else if (type().code >= Type.INT_DEC.code && type().code <= Type.INT_COLOR_RGB4.code) {
            return Integer.toString(data());
        }

        return "";
    }

    /**
     * Resource type codes.
     */
    public enum Type {
        /**
         * {@code data} is either 0 (undefined) or 1 (empty).
         */
        NULL(0x00),
        /**
         * {@code data} holds a {@link ResourceTableChunk} entry reference.
         */
        REFERENCE(0x01),
        /**
         * {@code data} holds an attribute resource identifier.
         */
        ATTRIBUTE(0x02),
        /**
         * {@code data} holds an index into the containing resource table's string pool.
         */
        STRING(0x03),
        /**
         * {@code data} holds a single-precision floating point number.
         */
        FLOAT(0x04),
        /**
         * {@code data} holds a complex number encoding a dimension value, such as "100in".
         */
        DIMENSION(0x05),
        /**
         * {@code data} holds a complex number encoding a fraction of a container.
         */
        FRACTION(0x06),
        /**
         * TODO: Unknown value form
         */
        DYNAMIC_ATTRIBUTE(0x08),
        /**
         * {@code data} holds a dynamic {@link ResourceTableChunk} entry reference.
         */
        DYNAMIC_REFERENCE(0x07),
        /**
         * {@code data} is a raw integer value of the form n..n.
         */
        INT_DEC(0x10),
        /**
         * {@code data} is a raw integer value of the form 0xn..n.
         */
        INT_HEX(0x11),
        /**
         * {@code data} is either 0 (false) or 1 (true).
         */
        INT_BOOLEAN(0x12),
        /**
         * {@code data} is a raw integer value of the form #aarrggbb.
         */
        INT_COLOR_ARGB8(0x1c),
        /**
         * {@code data} is a raw integer value of the form #rrggbb.
         */
        INT_COLOR_RGB8(0x1d),
        /**
         * {@code data} is a raw integer value of the form #argb.
         */
        INT_COLOR_ARGB4(0x1e),
        /**
         * {@code data} is a raw integer value of the form #rgb.
         */
        INT_COLOR_RGB4(0x1f);

        private static final Map<Byte, Type> FROM_BYTE;

        static {
            Builder<Byte, Type> builder = ImmutableMap.builder();
            for (Type type : values()) {
                builder.put(type.code(), type);
            }
            FROM_BYTE = builder.build();
        }

        private final byte code;

        Type(int code) {
            this.code = UnsignedBytes.checkedCast(code);
        }

        public static Type fromCode(byte code) {
            return Preconditions.checkNotNull(FROM_BYTE.get(code), "Unknown resource type: %s", code);
        }

        public byte code() {
            return code;
        }
    }
}
