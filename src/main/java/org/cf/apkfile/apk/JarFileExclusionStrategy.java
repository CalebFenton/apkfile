package org.cf.apkfile.apk;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

public class JarFileExclusionStrategy implements ExclusionStrategy {

    public boolean shouldSkipClass(Class<?> arg0) {
        return false;
    }

    public boolean shouldSkipField(FieldAttributes f) {
        // ZipFiles have a circular reference that makes serializing tricky.
        // JarEntries have a lot of noisy certificate information.
        return (f.getDeclaringClass() == JarFile.class || f.getDeclaringClass() == ZipFile.class || f.getDeclaringClass() == JarEntry.class);
    }
}
