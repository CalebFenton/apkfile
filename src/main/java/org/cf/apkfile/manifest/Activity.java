package org.cf.apkfile.manifest;

import org.cf.apkfile.res.AttributeId;
import org.cf.apkfile.res.XmlChunk;
import org.cf.apkfile.res.XmlStartElementChunk;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class Activity extends Component {

    // https://developer.android.com/guide/topics/manifest/activity-element.html
    // Enum value to string mappings: http://androidxref.com/7.1.1_r6/xref/frameworks/base/core/res/res/values/attrs_manifest.xml
    private final boolean allowEmbedded;
    private final boolean allowTaskReparenting;
    private final boolean alwaysRetainTaskState;
    private final boolean autoRemoveFromRecents;
    private final String banner;
    private final boolean clearTaskOnLaunch;
    private final int configChanges;
    private final int documentLaunchMode;
    private final boolean excludeFromRecents;
    private final boolean finishOnTaskLaunch;
    private final boolean hardwareAccelerated;
    private final int launchMode;
    private final int maxRecents;
    private final boolean multiprocess;
    private final boolean noHistory;
    private final String parentActivityName;
    private final int persistableMode;
    private final boolean relinquishTaskIdentity;
    private final boolean resizeableActivity;
    private final int screenOrientation;
    private final boolean showForAllUsers;
    private final boolean stateNotNeeded;
    private final boolean supportsPictureInPicture;
    private final String taskAffinity;
    private final String theme;
    private final int uiOptions;
    private final int windowSoftInputMode;

    Activity(XmlStartElementChunk activity, XmlChunk manifest, String packageName) throws XmlPullParserException, IOException {
        allowEmbedded = activity.getAttribute(AttributeId.ALLOW_EMBEDDED, false);
        allowTaskReparenting = activity.getAttribute(AttributeId.ALLOW_TASK_REPARENTING, false);
        alwaysRetainTaskState = activity.getAttribute(AttributeId.ALWAYS_RETAIN_TASK_STATE, false);
        autoRemoveFromRecents = activity.getAttribute(AttributeId.AUTO_REMOVE_FROM_RECENTS, false);
        banner = activity.getAttribute(AttributeId.BANNER);
        clearTaskOnLaunch = activity.getAttribute(AttributeId.CLEAR_TASK_ON_LAUNCH, false);
        configChanges = activity.getAttribute(AttributeId.CONFIG_CHANGES, 0);
        documentLaunchMode = activity.getAttribute(AttributeId.DOCUMENT_LAUNCH_MODE, 0);
        excludeFromRecents = activity.getAttribute(AttributeId.EXCLUDE_FROM_RECENTS, false);
        finishOnTaskLaunch = activity.getAttribute(AttributeId.FINISH_ON_TASK_LAUNCH, false);
        hardwareAccelerated = activity.getAttribute(AttributeId.HARDWARE_ACCELERATED, false);
        launchMode = activity.getAttribute(AttributeId.LAUNCH_MODE, 0);
        maxRecents = activity.getAttribute(AttributeId.MAX_RECENTS, 16);
        multiprocess = activity.getAttribute(AttributeId.MULTIPROCESS, false);
        noHistory = activity.getAttribute(AttributeId.NO_HISTORY, false);
        parentActivityName = activity.getAttribute(AttributeId.PARENT_ACTIVITY_NAME);
        persistableMode = activity.getAttribute(AttributeId.PERSISTABLE_MODE, 0);
        relinquishTaskIdentity = activity.getAttribute(AttributeId.RELINQUISH_TASK_IDENTITY, false);
        resizeableActivity = activity.getAttribute(AttributeId.RESIZEABLE_ACTIVITY, false);
        screenOrientation = activity.getAttribute(AttributeId.SCREEN_ORIENTATION, 0);
        showForAllUsers = activity.getAttribute(AttributeId.SHOW_FOR_ALL_USERS, false);
        stateNotNeeded = activity.getAttribute(AttributeId.STATE_NOT_NEEDED, false);
        supportsPictureInPicture = activity.getAttribute(AttributeId.SUPPORTS_PICTURE_IN_PICTURE, false);
        taskAffinity = activity.getAttribute(AttributeId.TASK_AFFINITY);
        theme = activity.getAttribute(AttributeId.THEME);
        uiOptions = activity.getAttribute(AttributeId.UI_OPTIONS, 0);
        windowSoftInputMode = activity.getAttribute(AttributeId.WINDOW_SOFT_INPUT_MODE, 0);
        parse(activity, manifest, packageName);
    }

    public String getBanner() {
        return banner;
    }

    public int getConfigChanges() {
        return configChanges;
    }

    public int getDocumentLaunchMode() {
        return documentLaunchMode;
    }

    public int getLaunchMode() {
        return launchMode;
    }

    public int getMaxRecents() {
        return maxRecents;
    }

    public String getParentActivityName() {
        return parentActivityName;
    }

    public int getScreenOrientation() {
        return screenOrientation;
    }

    public boolean getStateNotNeeded() {
        return stateNotNeeded;
    }

    public String getTaskAffinity() {
        return taskAffinity;
    }

    public String getTheme() {
        return theme;
    }

    public int getUiOptions() {
        return uiOptions;
    }

    public int getWindowSoftInputMode() {
        return windowSoftInputMode;
    }

    public boolean isAllowEmbedded() {
        return allowEmbedded;
    }

    public boolean isAllowTaskReparenting() {
        return allowTaskReparenting;
    }

    public boolean isAlwaysRetainTaskState() {
        return alwaysRetainTaskState;
    }

    public boolean isAutoRemoveFromRecents() {
        return autoRemoveFromRecents;
    }

    public boolean isClearTaskOnLaunch() {
        return clearTaskOnLaunch;
    }

    public boolean isExcludeFromRecents() {
        return excludeFromRecents;
    }

    public boolean isFinishOnTaskLaunch() {
        return finishOnTaskLaunch;
    }

    public boolean isHardwareAccelerated() {
        return hardwareAccelerated;
    }

    public boolean isMultiprocess() {
        return multiprocess;
    }

    public boolean isNoHistory() {
        return noHistory;
    }

    public boolean isRelinquishTaskIdentity() {
        return relinquishTaskIdentity;
    }

    public boolean isResizeableActivity() {
        return resizeableActivity;
    }

    public boolean isSupportsPictureInPicture() {
        return supportsPictureInPicture;
    }

    public int getPersistableMode() {
        return persistableMode;
    }

    public boolean isShowForAllUsers() {
        return showForAllUsers;
    }
}
