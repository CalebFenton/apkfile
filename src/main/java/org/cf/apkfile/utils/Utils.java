package org.cf.apkfile.utils;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Utils {
    private static final int SHA1_BUFF_LENGTH = 0x1000;
    private final static char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    public static Map<Object, Integer> convertToJava(TObjectIntMap map) {
        Map<Object, Integer> javaMap = new HashMap<>();
        for (Object key : map.keySet()) {
            javaMap.put(key, map.get(key));
        }
        return javaMap;
    }

    public static Map<Integer, Integer> convertToJava(TIntIntMap map) {
        Map<Integer, Integer> javaMap = new HashMap<>();
        for (int key : map.keys()) {
            javaMap.put(key, map.get(key));
        }
        return javaMap;
    }

    public static int leToBeI(int num) {
        return ((num << 24) & 0xff000000) | ((num << 8) & 0x00ff0000) | ((num >> 8) & 0x0000ff00) | ((num >> 24) & 0x000000ff);
    }

    public static short leToBeS(short num) {
        return (short) (((num >> 8) & 0x00ff) | ((num << 8) & 0xff00));
    }

    public static <E> Collection<E> makeCollection(Iterable<E> iter) {
        Collection<E> list = new LinkedList<E>();
        for (E item : iter) {
            list.add(item);
        }
        return list;
    }

    public static void rollUp(TIntIntMap dest, TIntIntMap src) {
        for (int key : src.keys()) {
            int value = src.get(key);
            dest.adjustOrPutValue(key, value, value);
        }
    }

    public static void rollUp(TObjectIntMap dest, TObjectIntMap src) {
        for (Object key : src.keySet()) {
            int value = src.get(key);
            dest.adjustOrPutValue(key, value, value);
        }
    }

    public static void rollUp(TObjectLongMap dest, TObjectLongMap src) {
        for (Object key : src.keySet()) {
            long value = src.get(key);
            dest.adjustOrPutValue(key, value, value);
        }
    }

    public static void updateAccessorCounts(TObjectIntMap<String> counts, int[] accessFlags) {
        for (int accessFlag : accessFlags) {
            if (Modifier.isPublic(accessFlag)) {
                counts.adjustOrPutValue("public", 1, 1);
            }
            if (Modifier.isProtected(accessFlag)) {
                counts.adjustOrPutValue("protected", 1, 1);
            }
            if (Modifier.isPrivate(accessFlag)) {
                counts.adjustOrPutValue("private", 1, 1);
            }
            if (Modifier.isFinal(accessFlag)) {
                counts.adjustOrPutValue("final", 1, 1);
            }
            if (Modifier.isInterface(accessFlag)) {
                counts.adjustOrPutValue("interface", 1, 1);
            }
            if (Modifier.isNative(accessFlag)) {
                counts.adjustOrPutValue("native", 1, 1);
            }
            if (Modifier.isStatic(accessFlag)) {
                counts.adjustOrPutValue("static", 1, 1);
            }
            if (Modifier.isStrict(accessFlag)) {
                counts.adjustOrPutValue("strict", 1, 1);
            }
            if (Modifier.isSynchronized(accessFlag)) {
                counts.adjustOrPutValue("synchronized", 1, 1);
            }
            if (Modifier.isTransient(accessFlag)) {
                counts.adjustOrPutValue("transient", 1, 1);
            }
            if (Modifier.isVolatile(accessFlag)) {
                counts.adjustOrPutValue("volatile", 1, 1);
            }
            if (Modifier.isAbstract(accessFlag)) {
                counts.adjustOrPutValue("abstract", 1, 1);
            }
        }
    }

    public static String getComponentBase(String classDescriptor) {
        /*
         * Because a class type reference may be an array, e.g.:
         * [Lcom/google/android/gms/internal/zzvk$zza$zza;->clone()Ljava/lang/Object;
         *
         * It's necessary to determine the "base" class when checking if a local class.
         */
        int index = 0;
        while (classDescriptor.charAt(index) == '[') {
            index += 1;
        }
        if (index == 0) {
            return classDescriptor;
        } else {
            return classDescriptor.substring(index);
        }
    }

    public static GsonBuilder getTroveAwareGsonBuilder() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        JsonSerializer<TObjectIntMap> objectIntMapJsonSerializer = (src, typeOfSrc, context) -> {
            JsonObject jsonMerchant = new JsonObject();
            for (Object key : src.keys()) {
                int value = src.get(key);
                jsonMerchant.addProperty(key.toString(), value);
            }
            return jsonMerchant;
        };
        gsonBuilder.registerTypeAdapter(TObjectIntMap.class, objectIntMapJsonSerializer);
        gsonBuilder.registerTypeAdapter(TObjectIntHashMap.class, objectIntMapJsonSerializer);

        JsonSerializer<TObjectLongMap> objectLongMapJsonSerializer = (src, typeOfSrc, context) -> {
            JsonObject jsonMerchant = new JsonObject();
            for (Object key : src.keys()) {
                long value = src.get(key);
                jsonMerchant.addProperty(key.toString(), value);
            }
            return jsonMerchant;
        };
        gsonBuilder.registerTypeAdapter(TObjectLongMap.class, objectLongMapJsonSerializer);
        gsonBuilder.registerTypeAdapter(TObjectLongHashMap.class, objectLongMapJsonSerializer);

        JsonSerializer<TIntIntMap> intIntMapJsonSerializer = (src, typeOfSrc, context) -> {
            JsonObject jsonMerchant = new JsonObject();
            for (int key : src.keys()) {
                int value = src.get(key);
                jsonMerchant.addProperty(Integer.toHexString(key), value);
            }
            return jsonMerchant;
        };
        gsonBuilder.registerTypeAdapter(TIntIntMap.class, intIntMapJsonSerializer);
        gsonBuilder.registerTypeAdapter(TIntIntHashMap.class, intIntMapJsonSerializer);

        return gsonBuilder;
    }

    public static byte[] readFully(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[0xffff];
        for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
            os.write(buffer, 0, len);
        }

        return os.toByteArray();
    }

    public static byte[] sha1(InputStream input) throws NoSuchAlgorithmException, IOException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        byte[] buff = new byte[SHA1_BUFF_LENGTH];
        int remaining = 0;
        while ((remaining = input.read(buff)) != -1) {
            sha1.update(buff, 0, remaining);
        }

        return sha1.digest();
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xff;
            hexChars[i * 2] = HEX_CHARS[v >>> 4];
            hexChars[i * 2 + 1] = HEX_CHARS[v & 0x0f];
        }
        return new String(hexChars);
    }

}
