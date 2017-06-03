package org.cf.apkfile.res;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;

/**
 * Describes a single resource entry.
 */
@AutoValue
public abstract class ResourceEntry {
    static ResourceEntry create(TypeChunk.Entry entry) {
        PackageChunk packageChunk = Preconditions.checkNotNull(entry.parent().getPackageChunk());
        String packageName = packageChunk.getPackageName();
        String typeName = entry.typeName();
        String entryName = entry.key();
        return new AutoValue_ResourceEntry(packageName, typeName, entryName);
    }

    public abstract String packageName();

    public abstract String typeName();

    public abstract String entryName();
}
