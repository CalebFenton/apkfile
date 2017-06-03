package org.cf.apkfile.manifest;

import org.cf.apkfile.res.AttributeId;
import org.cf.apkfile.res.Chunk;
import org.cf.apkfile.res.XmlChunk;
import org.cf.apkfile.res.XmlStartElementChunk;
import org.xmlpull.v1.XmlPullParserException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Application {

    private static final String ACTIVITY = "activity";
    private static final String ACTIVITY_ALIAS = "activity-alias";
    private static final String PROVIDER = "provider";
    private static final String RECEIVER = "receiver";
    private static final String SERVICE = "service";
    private static final String USES_LIBRARY = "uses-library";

    private final Collection<Activity> activities;
    private final Collection<ActivityAlias> activityAliases;
    private final boolean allowTaskReparenting;
    private final boolean allowBackup;
    private final String backupAgent;
    private final boolean backupInForeground;
    private final String banner;
    private final boolean debuggable;
    private final String description;
    private final boolean directBootAware;
    private final boolean enabled;
    private final boolean extractNativeLibs;
    private final String fullBackupContent;
    private final boolean fullBackupOnly;
    private final boolean hardwareAccelerated;
    private final boolean hasCode;
    private final String icon;
    private final boolean isGame;
    private final boolean killAfterRestore;
    private final boolean largeHeap;
    private final String label;
    private final String logo;
    private final String manageSpaceActivity;
    private final String name;
    private final String networkSecurityConfig;
    private final String permission;
    private final boolean persistent;
    private final String process;
    private final Collection<Provider> providers;
    private final Collection<Receiver> receivers;
    private final boolean restoreAnyVersion;
    private final String requiredAccountType;
    private final boolean resizableActivity;
    private final boolean supportsRtl;
    private final Collection<Service> services;
    private final String taskAffinity;
    private final String theme;
    private final int uiOptions;
    private final boolean usesCleartextTraffic;
    private final boolean vmSafeMode;
    private final Collection<UsesLibrary> usesLibraries;

    Application(XmlStartElementChunk application, XmlChunk manifest, String packageName) throws XmlPullParserException, IOException {
        activities = new LinkedList<>();
        activityAliases = new LinkedList<>();
        receivers = new LinkedList<>();
        services = new LinkedList<>();
        providers = new LinkedList<>();
        usesLibraries = new LinkedList<>();

        allowBackup = application.getAttribute(AttributeId.ALLOW_BACKUP, true);
        allowTaskReparenting = application.getAttribute(AttributeId.ALLOW_TASK_REPARENTING, false);
        backupAgent = application.getAttribute(AttributeId.BACKUP_AGENT);
        backupInForeground = application.getAttribute(AttributeId.BACKUP_IN_FOREGROUND, false);
        banner = application.getAttribute(AttributeId.BANNER);
        debuggable = application.getAttribute(AttributeId.DEBUGGABLE, false);
        description = application.getAttribute(AttributeId.DESCRIPTION);
        directBootAware = application.getAttribute(AttributeId.DIRECT_BOOT_AWARE, false);
        enabled = application.getAttribute(AttributeId.ENABLED, true);
        extractNativeLibs = application.getAttribute(AttributeId.EXTRACT_NATIVE_LIBS, true);
        fullBackupContent = application.getAttribute(AttributeId.FULL_BACKUP_CONTENT);
        fullBackupOnly = application.getAttribute(AttributeId.FULL_BACKUP_ONLY, false);
        hasCode = application.getAttribute(AttributeId.HAS_CODE, true);
        hardwareAccelerated = application.getAttribute(AttributeId.HARDWARE_ACCELERATED, true);
        icon = application.getAttribute(AttributeId.ICON);
        isGame = application.getAttribute(AttributeId.IS_GAME, false);
        killAfterRestore = application.getAttribute(AttributeId.KILL_AFTER_RESTORE, true);
        largeHeap = application.getAttribute(AttributeId.LARGE_HEAP, false);
        label = application.getAttribute(AttributeId.LABEL);
        logo = application.getAttribute(AttributeId.LOGO);
        manageSpaceActivity = application.getAttribute(AttributeId.MANAGE_SPACE_ACTIVITY);
        String appName = application.getAttribute(AttributeId.NAME);
        name = Utils.ensureFullName(appName, packageName);
        networkSecurityConfig = application.getAttribute(AttributeId.NETWORK_SECURITY_CONFIG);
        permission = application.getAttribute(AttributeId.PERMISSION);
        persistent = application.getAttribute(AttributeId.PERSISTENT, false);
        process = application.getAttribute(AttributeId.PROCESS);
        restoreAnyVersion = application.getAttribute(AttributeId.RESTORE_ANY_VERSION, false);
        requiredAccountType = application.getAttribute(AttributeId.REQUIRED_ACCOUNT_TYPE);
        resizableActivity = application.getAttribute(AttributeId.RESIZEABLE_ACTIVITY, false);
        supportsRtl = application.getAttribute(AttributeId.SUPPORTS_RTL, false);
        taskAffinity = application.getAttribute(AttributeId.TASK_AFFINITY);
        theme = application.getAttribute(AttributeId.THEME);
        uiOptions = application.getAttribute(AttributeId.UI_OPTIONS, 0);
        usesCleartextTraffic = application.getAttribute(AttributeId.USES_CLEARTEXT_TRAFFIC, true);
        vmSafeMode = application.getAttribute(AttributeId.VM_SAFE_MODE, false);

        List<Chunk> applicationChunks = manifest.getChunksWithin(application);
        for (Chunk c : applicationChunks) {
            if (!(c instanceof XmlStartElementChunk)) {
                continue;
            }

            XmlStartElementChunk chunk = (XmlStartElementChunk) c;
            String name = chunk.getName();
            if (ACTIVITY.equals(name)) {
                activities.add(new Activity(chunk, manifest, packageName));
            } else if (ACTIVITY_ALIAS.equals(name)) {
                activityAliases.add(new ActivityAlias(chunk, manifest, packageName));
            } else if (PROVIDER.equals(name)) {
                providers.add(new Provider(chunk, manifest, packageName));
            } else if (RECEIVER.equals(name)) {
                receivers.add(new Receiver(chunk, manifest, packageName));
            } else if (SERVICE.equals(name)) {
                services.add(new Service(chunk, manifest, packageName));
            } else if (USES_LIBRARY.equals(name)) {
                usesLibraries.add(new UsesLibrary(chunk));
            }
        }
    }

    public Collection<Activity> getActivities() {
        return activities;
    }

    public Collection<ActivityAlias> getActivityAliases() {
        return activityAliases;
    }

    public String getBackupAgent() {
        return backupAgent;
    }

    public String getBanner() {
        return banner;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    public String getLabel() {
        return label;
    }

    public String getLogo() {
        return logo;
    }

    public
    @Nullable
    Activity getMainLauncher() {
        Activity mainLauncher = null;
        outer:
        for (Activity activity : activities) {
            boolean hasMain = false;
            boolean hasLauncher = false;
            for (IntentFilter intentFilter : activity.getIntentFilters()) {
                if (intentFilter.getActions().contains("android.intent.action.MAIN")) {
                    hasMain = true;
                }
                if (intentFilter.getCategories().contains("android.intent.category.LAUNCHER")) {
                    hasLauncher = true;
                }

                if (hasMain && hasLauncher) {
                    mainLauncher = activity;
                    break outer;
                }
            }
        }

        return mainLauncher;
    }

    public String getManageSpaceActivity() {
        return manageSpaceActivity;
    }

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public String getProcess() {
        return process;
    }

    public Collection<Provider> getProviders() {
        return providers;
    }

    public Collection<Receiver> getReceivers() {
        return receivers;
    }

    public Collection<Service> getServices() {
        return services;
    }

    public String getTaskAffinity() {
        return taskAffinity;
    }

    public String getTheme() {
        return theme;
    }

    public Collection<UsesLibrary> getUsesLibraries() {
        return usesLibraries;
    }

    public boolean isAllowBackup() {
        return allowBackup;
    }

    public boolean isAllowTaskReparenting() {
        return allowTaskReparenting;
    }

    public boolean isBackupInForeground() {
        return backupInForeground;
    }

    public boolean isDebuggable() {
        return debuggable;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isExtractNativeLibs() {
        return extractNativeLibs;
    }

    public boolean isGame() {
        return isGame;
    }

    public boolean isHardwareAccelerated() {
        return hardwareAccelerated;
    }

    public boolean isHasCode() {
        return hasCode;
    }

    public boolean isKillAfterRestore() {
        return killAfterRestore;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public boolean isRestoreAnyVersion() {
        return restoreAnyVersion;
    }

    public boolean isDirectBootAware() {
        return directBootAware;
    }

    public String getNetworkSecurityConfig() {
        return networkSecurityConfig;
    }

    public boolean isLargeHeap() {
        return largeHeap;
    }

    public boolean isVmSafeMode() {
        return vmSafeMode;
    }

    public boolean isUsesCleartextTraffic() {
        return usesCleartextTraffic;
    }

    public int getUiOptions() {
        return uiOptions;
    }

    public boolean isResizableActivity() {
        return resizableActivity;
    }

    public String getRequiredAccountType() {
        return requiredAccountType;
    }

    public boolean isSupportsRtl() {
        return supportsRtl;
    }
}
