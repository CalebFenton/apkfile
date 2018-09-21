package org.cf.apkfile.manifest;

import com.google.common.io.ByteStreams;
import org.cf.apkfile.res.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class AndroidManifest {

    private static final transient Logger logger = LoggerFactory.getLogger(AndroidManifest.class);

    private static final String APPLICATION_ELEMENT = "application";
    private static final String COMPATIBLE_SCREENS = "compatible-screens";
    private static final String INSTRUMENTATION = "instrumentation";
    private static final String MANIFEST_ELEMENT = "manifest";
    private static final String PACKAGE = "package";
    private static final String PERMISSION = "permission";
    private static final String PERMISSION_GROUP = "permission-group";
    private static final String PERMISSION_TREE = "permission-tree";
    private static final String PLATFORM_BUILD_VERSION_CODE = "platformBuildVersionCode";
    private static final String PLATFORM_BUILD_VERSION_NAME = "platformBuildVersionName";
    private static final String SCREEN = "screen";
    private static final String SUPPORTS_GL_TEXTURE = "supports-gl-texture";
    private static final String SUPPORTS_SCREENS = "supports-screens";
    private static final String USES_CONFIGURATION = "uses-configuration";
    private static final String USES_FEATURE = "uses-feature";
    private static final String USES_PERMISSION = "uses-permission";
    private static final String USES_SDK = "uses-sdk";

    private Application application;
    private List<Screen> compatibleScreens;
    private boolean hasResources;
    private int installLocation;
    private List<Instrumentation> instrumentations;
    private int maxSdkVersion;
    private int minSdkVersion;
    private String packageName;
    private List<PermissionGroup> permissionGroups;
    private List<PermissionTree> permissionTrees;
    private List<Permission> permissions;
    private int platformBuildVersionCode;
    private String platformBuildVersionName;
    private String sharedUserId;
    private String sharedUserLabel;
    private List<String> supportsGlTextures;
    private SupportsScreens supportsScreens;
    private int targetSdkVersion;
    private List<UsesConfiguration> usesConfigurations;
    private List<UsesFeature> usesFeatures;
    private List<String> usesPermissions;
    private int versionCode;
    private String versionName;

    public AndroidManifest(InputStream manifestStream) throws AndroidManifestParsingException {
        initialize(manifestStream, null);
    }

    public AndroidManifest(InputStream manifestStream,
                           @Nullable ResourceTableChunk resourceTable) throws AndroidManifestParsingException {
        initialize(manifestStream, resourceTable);
    }

    public Application getApplication() {
        return application;
    }

    public Collection<Screen> getCompatibleScreens() {
        return compatibleScreens;
    }

    public int getInstallLocation() {
        return installLocation;
    }

    public Collection<Instrumentation> getInstrumentations() {
        return instrumentations;
    }

    public int getMaxSdkVersion() {
        return maxSdkVersion;
    }

    public int getMinSdkVersion() {
        return this.minSdkVersion;
    }

    public String getPackageName() {
        return packageName;
    }

    public Collection<PermissionGroup> getPermissionGroups() {
        return permissionGroups;
    }

    public Collection<PermissionTree> getPermissionTrees() {
        return permissionTrees;
    }

    public Collection<Permission> getPermissions() {
        return permissions;
    }

    public int getPlatformBuildVersionCode() {
        return platformBuildVersionCode;
    }

    public String getPlatformBuildVersionName() {
        return platformBuildVersionName;
    }

    public String getSharedUserId() {
        return sharedUserId;
    }

    public String getSharedUserLabel() {
        return sharedUserLabel;
    }

    public Collection<String> getSupportsGlTextures() {
        return supportsGlTextures;
    }

    public SupportsScreens getSupportsScreens() {
        return supportsScreens;
    }

    public int getTargetSdkVersion() {
        return targetSdkVersion;
    }

    public Collection<UsesConfiguration> getUsesConfigurations() {
        return usesConfigurations;
    }

    public Collection<UsesFeature> getUsesFeatures() {
        return usesFeatures;
    }

    public Collection<String> getUsesPermissions() {
        return usesPermissions;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public boolean hasResources() {
        return hasResources;
    }

    private void initialize(InputStream manifestStream,
                            @Nullable ResourceTableChunk resourceTable) throws AndroidManifestParsingException {
        compatibleScreens = new LinkedList<>();
        instrumentations = new LinkedList<>();
        permissions = new LinkedList<>();
        permissionGroups = new LinkedList<>();
        permissionTrees = new LinkedList<>();
        supportsGlTextures = new LinkedList<>();
        usesConfigurations = new LinkedList<>();
        usesFeatures = new LinkedList<>();
        usesPermissions = new LinkedList<>();

        try {
            parse(manifestStream, resourceTable);
        } catch (Exception e) {
            throw new AndroidManifestParsingException(e);
        }
    }

    private void parse(InputStream manifestStream,
                       @Nullable ResourceTableChunk resourceTable) throws XmlPullParserException, IOException {
        hasResources = resourceTable != null;
        byte[] buf = ByteStreams.toByteArray(manifestStream);
        ByteBuffer buffer = ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN);
        XmlChunk manifest = (XmlChunk) Chunk.newInstance(buffer, resourceTable);
        manifest.setResourceTable(resourceTable);

        for (Chunk c : manifest.getChunks().values()) {
            if (c instanceof XmlStartElementChunk) {
                XmlStartElementChunk chunk = (XmlStartElementChunk) c;
                String name = chunk.getName();
                if (MANIFEST_ELEMENT.equals(name)) {
                    if (packageName != null) {
                        logger.warn("Multiple manifests elements in AndroidManifest; ignoring");
                    } else {
                        parsePackageInformation(chunk);
                    }
                } else if (APPLICATION_ELEMENT.equals(name)) {
                    application = new Application(chunk, manifest, packageName);
                } else if (COMPATIBLE_SCREENS.equals(name)) {
                    parseCompatibleScreens(chunk, manifest);
                } else if (INSTRUMENTATION.equals(name)) {
                    instrumentations.add(new Instrumentation(chunk));
                } else if (PERMISSION.equals(name)) {
                    permissions.add(new Permission(chunk));
                } else if (PERMISSION_GROUP.equals(name)) {
                    permissionGroups.add(new PermissionGroup(chunk));
                } else if (PERMISSION_TREE.equals(name)) {
                    permissionTrees.add(new PermissionTree(chunk));
                } else if (SUPPORTS_SCREENS.equals(name)) {
                    supportsScreens = new SupportsScreens(chunk);
                } else if (SUPPORTS_GL_TEXTURE.equals(name)) {
                    supportsGlTextures.add(chunk.getAttribute(AttributeId.NAME));
                } else if (USES_CONFIGURATION.equals(name)) {
                    usesConfigurations.add(new UsesConfiguration(chunk));
                } else if (USES_FEATURE.equals(name)) {
                    usesFeatures.add(new UsesFeature(chunk));
                } else if (USES_PERMISSION.equals(name)) {
                    usesPermissions.add(chunk.getAttribute(AttributeId.NAME));
                } else if (USES_SDK.equals(name)) {
                    parseUsesSdk(chunk);
                }
            }
        }
    }

    private void parseUsesSdk(XmlStartElementChunk usesSdk) {
        String value = usesSdk.getAttribute(AttributeId.MIN_SDK_VERSION);
        if (value != null && !value.isEmpty()) {
            try {
                minSdkVersion = Integer.parseInt(value);
            } catch (NumberFormatException nfe) {
                logger.warn("Invalid minSdkVersion value: " + value);
            }
        }
        value = usesSdk.getAttribute(AttributeId.TARGET_SDK_VERSION);
        if (value != null && !value.isEmpty()) {
            try {
                targetSdkVersion = Integer.parseInt(value);
            } catch (NumberFormatException nfe) {
                logger.warn("Invalid targetSdkVersion value: " + value);
            }
        }
        value = usesSdk.getAttribute(AttributeId.MAX_SDK_VERSION);
        if (value != null && !value.isEmpty()) {
            try {
                maxSdkVersion = Integer.parseInt(value);
            } catch (NumberFormatException nfe) {
                logger.warn("Invalid maxSdkVersion value: " + value);
            }
        }
    }

    private void parseCompatibleScreens(XmlStartElementChunk screens, XmlChunk manifest) throws XmlPullParserException, IOException {
        List<Chunk> compatibleScreenChunks = manifest.getChunksWithin(screens);
        for (Chunk c : compatibleScreenChunks) {
            if (!(c instanceof XmlStartElementChunk)) {
                continue;
            }
            XmlStartElementChunk chunk = (XmlStartElementChunk) c;
            String name = chunk.getName();
            if (SCREEN.equals(name)) {
                compatibleScreens.add(new Screen(chunk));
            }
        }
    }

    private void parsePackageInformation(XmlStartElementChunk manifest) {
        // Enum value to string mappings: http://androidxref.com/7.1.1_r6/xref/frameworks/base/core/res/res/values/attrs_manifest.xml
        installLocation = manifest.getAttribute(AttributeId.INSTALL_LOCATION, 0);
        packageName = manifest.getAttribute(PACKAGE);
        platformBuildVersionCode = manifest
                .getAttribute(PLATFORM_BUILD_VERSION_CODE, -1);
        platformBuildVersionName = manifest.getAttribute(PLATFORM_BUILD_VERSION_NAME);
        sharedUserId = manifest.getAttribute(AttributeId.SHARED_USER_ID);
        sharedUserLabel = manifest.getAttribute(AttributeId.SHARED_USER_LABEL);
        versionCode = manifest.getAttribute(AttributeId.VERSION_CODE, -1);
        versionName = manifest.getAttribute(AttributeId.VERSION_NAME);
    }
}
