package com.nhance.android.managers;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.Tracker;
import com.nhance.android.activities.LoginActivity;
import com.nhance.android.async.tasks.ActivityRecoderTask;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.models.Organization;
import com.nhance.android.db.models.UserOrgProfile;
import com.nhance.android.db.models.entity.User;
import com.nhance.android.enums.EntityType;
import com.nhance.android.factory.CMDSUrlFactory;
import com.nhance.android.pojos.OrgMemberInfo;
import com.nhance.android.pojos.OrgMemberMappingInfo.OrgProgramCenterSectionIds;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.VedantuWebUtils;

public class SessionManager {

    private static final String   TAG               = "SessionManager";
    private Context               _context;
    int                           PRIVATE_MODE      = 0;

    private static final String   PREF_DEFAULT_NAME = "VedantuLoginPref";
    private static final String   PREF_NAME_SYNC    = "SyncPref";

    private static final String   IS_LOGIN          = "isLoggedIn";
    private static SessionManager INSTANCE          = null;
    private static Properties     properties        = new Properties();

    private OrgMemberInfo         orgMemberInfo;

    public static SessionManager getInstance(Context context) {

        if (INSTANCE == null) {
            INSTANCE = new SessionManager(context);
        }
        return INSTANCE;
    }

    @SuppressLint("CommitPrefEdits")
    private SessionManager(Context context) {

        this._context = context;
        online = VedantuWebUtils.isOnline(_context);
        try {
            properties.load(context.getAssets().open("conf/app.conf"));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }

    public static String getCMDSUrlFromEnvMode(String envMode, Context context) {

        String cmdsUrl = null;
        Log.d(TAG, "envMode : " + envMode);
        String key = TextUtils.isEmpty(envMode) ? "cmdsUrl" : (envMode + ".cmdsUrl");
        Log.d(TAG, "property key : " + key);
        cmdsUrl = properties.getProperty(key);

        return cmdsUrl;
    }

    public static String getConfigProperty(String key) {

        return properties.getProperty(key, StringUtils.EMPTY);
    }

    public Context getContext() {

        return _context;
    }

    /**
     * Create login session and save the session data to SharedPreferences
     * */

    public OrgMemberInfo getOrgMemberInfo() {

        if (orgMemberInfo != null) {
            return orgMemberInfo;
        }
        String orgProfile = getSessionSharedPreferences().getString(ConstantGlobal.ORG_PROFILE, "");
        if (!TextUtils.isEmpty(orgProfile)) {
            try {
                JSONObject orgInfoJSON = new JSONObject(orgProfile);
                orgMemberInfo = new OrgMemberInfo();
                orgMemberInfo.fromJSON(orgInfoJSON);
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return orgMemberInfo;

    }

    public void createLoginSession(Organization org, User user, UserOrgProfile userOrgProfile) {

        Editor editor = getSessionSharedPreferences().edit();

        editor.putBoolean(IS_LOGIN, true);

        editor.putString(ConstantGlobal.USERNAME, user.username);

        editor.putString(ConstantGlobal.FIRST_NAME, user.firstName);
        editor.putString(ConstantGlobal.LAST_NAME, user.lastName);
        editor.putString(ConstantGlobal.USER_ID, user.userId);
        editor.putBoolean("autoLogin", user.autoLogin);
        editor.putString(ConstantGlobal.ORG_ID, org.orgId);
        editor.putString(ConstantGlobal.ORG_NAME, org.name);
        editor.putInt(ConstantGlobal.ORG_KEY_ID, org._id);
        editor.putString(ConstantGlobal.ORG_PROFILE,
                JSONUtils.getJSONObject(userOrgProfile.orgProfile, "info").toString());
        editor.putString(ConstantGlobal.CMDS_URL, org.cmdsUrl);
        editor.commit();
        orgMemberInfo = null;
        getOrgMemberInfo();
    }

    public void createOrgSession(Organization org) {

        Editor editor = getSessionSharedPreferences().edit();
        editor.putString(ConstantGlobal.ORG_ID, org.orgId);
        editor.putString(ConstantGlobal.ORG_NAME, org.name);
        editor.putInt(ConstantGlobal.ORG_KEY_ID, org._id);
        editor.putString(ConstantGlobal.CMDS_URL, org.cmdsUrl);
        editor.commit();
    }

    public void updateUserOrgProfile(UserOrgProfile userOrgProfile) {

        Editor editor = getSessionSharedPreferences().edit();
        editor.putString(ConstantGlobal.ORG_PROFILE,
                JSONUtils.getJSONObject(userOrgProfile.orgProfile, "info").toString());
        editor.commit();
        orgMemberInfo = null;
        orgMemberInfo = getOrgMemberInfo();
    }

    /**
     * Check login method will check user login status If false it will redirect user to login page
     * Else won't do anything
     * */
    public boolean checkLogin() {

        // Check login status
        if (!this.isLoggedIn()) {
            // user is not logged in redirect him to Login Activity
            Intent intent = new Intent(_context, LoginActivity.class);

            // Closing all the Activities
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(intent);
        }
        return this.isLoggedIn();
    }

    /**
     * Clear session details
     * */
    public void logoutUser() {

        Map<String, Object> httpParams = new HashMap<String, Object>();
        addSessionParams(httpParams);

        new ActivityRecoderTask(httpParams, _context,
                getSessionStringValue(ConstantGlobal.CMDS_URL)).recordLogout();

        // Clearing all data from Shared Preferences
        Editor editor = getSessionSharedPreferences().edit();
        editor.clear();
        editor.commit();
        orgMemberInfo = null;
        INSTANCE = null;
        Log.d(TAG, "cleared session");
        // clearing GA user info custom dimensions
        captureUserInfoInGACustomDimensions();
        // After logout redirect user to Loing Activity
        Intent i = new Intent(_context, LoginActivity.class);
        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        _context.startActivity(i);

    }

    public void recordActivity(String page, String userAction, String entityId,
            EntityType entityType, int localActivityId) {

        Map<String, Object> httpParams = new HashMap<String, Object>();
        addSessionParams(httpParams);
        httpParams.put("page", page);
        httpParams.put("userAction", userAction);
        httpParams.put("entity.type", entityType.name());
        httpParams.put("entity.id", entityId);
        httpParams.put("activityTime", Calendar.getInstance().getTimeInMillis());
        new ActivityRecoderTask(httpParams, _context,
                getSessionStringValue(ConstantGlobal.CMDS_URL)).recordActivity(localActivityId);
    }

    // set and unset google analytics custom dimensions
    public void captureUserInfoInGACustomDimensions() {

        Tracker tracker = VApp.getGaTracker();
        // it is mentioned in documentation that a new session has to be set
        // before
        // setting new user/sesssion scope values to make sure that
        // they affect the new data and not the old data
        tracker.set(Fields.customDimension(ConstantGlobal.GA_CUSTOM_DIMENSION_USERID_INDEX),
                getSessionStringValue(ConstantGlobal.USER_ID));

        String firstName = getSessionStringValue(ConstantGlobal.FIRST_NAME) == null ? ConstantGlobal.EMPTY
                : getSessionStringValue(ConstantGlobal.FIRST_NAME);
        String lastName = getSessionStringValue(ConstantGlobal.LAST_NAME) == null ? ConstantGlobal.EMPTY
                : getSessionStringValue(ConstantGlobal.LAST_NAME);

        if (StringUtils.isEmpty(firstName) && StringUtils.isEmpty(lastName)) {
            tracker.set(Fields.customDimension(ConstantGlobal.GA_CUSTOM_DIMENSION_FULLNAME_INDEX),
                    null);
        } else {
            tracker.set(Fields.customDimension(ConstantGlobal.GA_CUSTOM_DIMENSION_FULLNAME_INDEX),
                    (firstName + " " + lastName));
        }
        tracker.set(Fields.customDimension(ConstantGlobal.GA_CUSTOM_DIMENSION_ORGID_INDEX),
                getSessionStringValue(ConstantGlobal.ORG_ID));
        tracker.set(Fields.customDimension(ConstantGlobal.GA_CUSTOM_DIMENSION_ORG_NAME_INDEX),
                getSessionStringValue(ConstantGlobal.ORG_NAME));
    }

    // Get Login State
    public boolean isLoggedIn() {

        return getSessionSharedPreferences().getBoolean(IS_LOGIN, false);
    }

    public String getSessionStringValue(String key) {

        return getSessionSharedPreferences().getString(key, StringUtils.EMPTY);
    }

    public int getSessionIntValue(String key) {

        return getSessionSharedPreferences().getInt(key, 0);
    }

    public boolean getSessionBooleanValue(String key) {

        return getSessionSharedPreferences().getBoolean(key, false);
    }

    private SharedPreferences getSessionSharedPreferences() {

        return _context.getSharedPreferences(PREF_DEFAULT_NAME, PRIVATE_MODE);
    }

    private SharedPreferences getSyncSharedPreferences() {

        return _context.getSharedPreferences(PREF_NAME_SYNC, PRIVATE_MODE);
    }

    public void addSessionParams(Map<String, Object> params) {

        SharedPreferences pref = getSessionSharedPreferences();
        if (params != null) {
            params.put(ConstantGlobal.USER_ID, pref.getString(ConstantGlobal.USER_ID, null));
            params.put("callingUserId", pref.getString(ConstantGlobal.USER_ID, null));
            params.put("targetUserId", pref.getString(ConstantGlobal.USER_ID, null));
            params.put(ConstantGlobal.ORG_ID, pref.getString(ConstantGlobal.ORG_ID, null));
            OrgMemberInfo orgMemberInfo = getOrgMemberInfo();
            if (orgMemberInfo != null) {
                params.put("memberId", orgMemberInfo.memberId);
                params.put("profile", orgMemberInfo.profile);
            }
            addDeviceInfo(params, _context);
        }
    }

    public void addSessionParams1(Map<String, Object> params) {

        SharedPreferences pref = getSessionSharedPreferences();
        if (params != null) {
            params.put(ConstantGlobal.USER_ID, pref.getString(ConstantGlobal.USER_ID, null));
            params.put("callingUserId", pref.getString(ConstantGlobal.USER_ID, null));
            //params.put("targetUserId", pref.getString(ConstantGlobal.USER_ID, null));
            params.put(ConstantGlobal.ORG_ID, pref.getString(ConstantGlobal.ORG_ID, null));
            OrgMemberInfo orgMemberInfo = getOrgMemberInfo();
            if (orgMemberInfo != null) {
                params.put("memberId", orgMemberInfo.memberId);
                params.put("profile", orgMemberInfo.profile);
            }
            addDeviceInfo(params, _context);
        }
    }

    public void addContentSrcParams(Map<String, Object> params) {

        params.put("contentSrc.id", getSessionStringValue(ConstantGlobal.ORG_ID));
        params.put("contentSrc.type", "ORGANIZATION");
    }

    public static void addDeviceInfo(Map<String, Object> params, Context context) {

        WifiManager wifiM = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        params.put("mac", wifiM.getConnectionInfo().getMacAddress());
        params.put("deviceId", wifiM.getConnectionInfo().getMacAddress() == null ? "TABAPP" : wifiM
                .getConnectionInfo().getMacAddress());
        params.put("deviceType", "MOBILE");
        params.put("versionCode", getVersionCode(context, true));
        params.put(Settings.Secure.ANDROID_ID,
                Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID));
    }

    public static String getMacAddress(Context context) {

        WifiManager wifiM = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wifiM.getConnectionInfo().getMacAddress() == null ? "TABAPP" : wifiM
                .getConnectionInfo().getMacAddress();
    }

    static int versionCode = 0;

    public static int getVersionCode(Context context, boolean fromCache) {

        if (fromCache && versionCode != 0) {
            return versionCode;
        }
        try {
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            // Huh? Really?
        }
        return versionCode;
    }

    public String getApiUrl(String api) {

        return CMDSUrlFactory.INSTANCE.getCMDSUrl(SessionManager.getInstance(_context)
                .getSessionStringValue(ConstantGlobal.CMDS_URL), api);
    }

    public void updateLibraryLatestContentTime(long time, String targetId, String targetType) {

        if (time == 0) {
            return;
        }

        SharedPreferences libSyncPref = getSyncSharedPreferences();
        Editor libSyncEditor = libSyncPref.edit();
        String userId = getSessionStringValue(ConstantGlobal.USER_ID);
        libSyncEditor.putLong(getLatestContentTimeKey(userId, targetId, targetType) + "_old",
                libSyncPref.getLong(getLatestContentTimeKey(userId, targetId, targetType), 0));
        libSyncEditor.putLong(getLatestContentTimeKey(userId, targetId, targetType), time);
        libSyncEditor.commit();
    }

    public long getLatestLibContentTime(String targetId, String targetType) {

        return getSyncSharedPreferences().getLong(
                getLatestContentTimeKey(getSessionStringValue(ConstantGlobal.USER_ID), targetId,
                        targetType), 0);
    }

    public void updateContentLastSyncTime(long time, String targetType, String targetId) {

        SharedPreferences libSyncPref = getSyncSharedPreferences();
        Editor libSyncEditor = libSyncPref.edit();
        String userId = getSessionStringValue(ConstantGlobal.USER_ID);
        libSyncEditor.putLong(getSyncKey(userId, targetId, targetType) + "_old",
                libSyncPref.getLong(getSyncKey(userId, targetId, targetType), 0));
        libSyncEditor.putLong(getSyncKey(userId, targetId, targetType), time);
        libSyncEditor.commit();
    }

    public long getContentLastSyncTime(String targetId, String targetType) {

        return getSyncSharedPreferences().getLong(
                getSyncKey(getSessionStringValue(ConstantGlobal.USER_ID), targetId, targetType), 0);
    }

    public void updateLatestAnalyticsTime(long time) {

        SharedPreferences syncPref = getSyncSharedPreferences();
        Editor syncEditor = syncPref.edit();
        String userId = getSessionStringValue(ConstantGlobal.USER_ID);

        if (time == 0 || syncPref.getLong(getLatestAnalyticsSyncKey(userId), 0) > time) {
            return;
        }

        syncEditor.putLong(getLatestAnalyticsSyncKey(userId) + "_old",
                syncPref.getLong(getLatestAnalyticsSyncKey(userId), 0));
        syncEditor.putLong(getLatestAnalyticsSyncKey(userId), time);
        syncEditor.commit();
    }

    public long getLatestAnalyticsTime() {

        return getSyncSharedPreferences().getLong(
                getLatestAnalyticsSyncKey(getSessionStringValue(ConstantGlobal.USER_ID)), 0);
    }

    public void updateAnalyticsLastSyncTime(long time) {

        SharedPreferences syncPref = getSyncSharedPreferences();
        Editor syncEditor = syncPref.edit();
        String userId = getSessionStringValue(ConstantGlobal.USER_ID);
        syncEditor.putLong(getAnalyticsSyncKey(userId) + "_old",
                syncPref.getLong(getAnalyticsSyncKey(userId), 0));
        syncEditor.putLong(getAnalyticsSyncKey(userId), time);
        syncEditor.commit();
    }

    public long getAnalyticsLastSyncTime() {

        return getSyncSharedPreferences().getLong(
                getAnalyticsSyncKey(getSessionStringValue(ConstantGlobal.USER_ID)), 0);
    }

    private String getLatestContentTimeKey(String userId, String targetId, String targetType) {

        return LATEST_CONTENT + targetType + targetId + userId;
    }

    private String getSyncKey(String targetType, String targetId, String userId) {

        return ConstantGlobal.LAST_SYNC + userId + "_" + targetType + "_" + targetId;
    }

    private String getLatestAnalyticsSyncKey(String userId) {

        return "latest_analytics_" + userId;
    }

    private String getAnalyticsSyncKey(String userId) {

        return "analytics_" + userId;
    }

    public static boolean isOnline() {

        return online;
    }

    public static synchronized void setOnline(boolean online) {

        SessionManager.online = online;
    }

    public static String getCMDSUrlForWhiteLabeledOrg() {

        return properties.getProperty("whiteLabeledCmdsUrl");
    }

    public static void addListParams(List<? extends Object> values, String keyPrefix,
            Map<String, Object> httpParams) {

        if (httpParams != null && values != null) {
            for (int i = 0; i < values.size(); i++) {
                httpParams.put(keyPrefix + "[" + i + "]", values.get(i));
            }
        }
    }

    // public void putStringSetSharedPref(String key, Set<String> values) {
    //
    // getSessionSharedPreferences().edit().putStringSet(key, values).commit();
    // }

    private final String   LATEST_CONTENT = "latestContent";
    private static boolean online         = false;

    public void setShowedContentLoadToastOnLogin() {

        Editor editor = getSessionSharedPreferences().edit();

        editor.putBoolean("showedContentLoadToastOnLogin", true);
        editor.commit();
    }

    public boolean getShowedContentLoadToastOnLogin() {

        return getSessionSharedPreferences().getBoolean("showedContentLoadToastOnLogin", false);
    }

    public void setCurrentSectionDetails(String sectionId, String progCenterSecName) {

        Editor editor = getSessionSharedPreferences().edit();

        editor.putString(ConstantGlobal.CURRENT_SECTION_ID, sectionId);
        editor.putString(ConstantGlobal.CURRENT_SECTION_NAME, progCenterSecName);
        editor.commit();
    }

    public void setCurrentProgramCenterSectionIds(OrgProgramCenterSectionIds orgIds) {

        Editor editor = getSessionSharedPreferences().edit();
        editor.putString(ConstantGlobal.CURRENT_SECTION_ID, orgIds.sectionId);
        editor.putString(ConstantGlobal.CURRENT_CENTER_ID, orgIds.centerId);
        editor.putString(ConstantGlobal.CURRENT_PROGRAM_ID, orgIds.programId);
        editor.commit();
    }

    public void setCurrentProgramCenterSectionIds(String programId, String centerId,
            String sectionId) {

        Editor editor = getSessionSharedPreferences().edit();
        editor.putString(ConstantGlobal.CURRENT_SECTION_ID, sectionId);
        editor.putString(ConstantGlobal.CURRENT_CENTER_ID, centerId);
        editor.putString(ConstantGlobal.CURRENT_PROGRAM_ID, programId);
        editor.commit();
    }

    public OrgProgramCenterSectionIds getCurrentProgramCenterSectionIds() {

        SharedPreferences data = getSessionSharedPreferences();
        String programId = data.getString(ConstantGlobal.CURRENT_PROGRAM_ID, "");
        String centerId = data.getString(ConstantGlobal.CURRENT_CENTER_ID, "");
        String sectionId = data.getString(ConstantGlobal.CURRENT_SECTION_ID, "");
        OrgProgramCenterSectionIds orgIds = new OrgProgramCenterSectionIds(programId, centerId,
                sectionId);
        return orgIds;
    }

    private static final String CURRENT_CARD_ID = "currentCardId";

    public void setCurrentCardId(String cardId) {

        Editor editor = getSessionSharedPreferences().edit();
        editor.putString(CURRENT_CARD_ID, cardId);
        editor.commit();
    }

    public void removeCurrentCardId() {

        Editor editor = getSessionSharedPreferences().edit();
        editor.remove(CURRENT_CARD_ID);
        editor.commit();
    }

    public String getCurrentCardId() {

        return getSessionSharedPreferences().getString(CURRENT_CARD_ID, StringUtils.EMPTY);
    }

    private static final String COMMENTS_COUNT = "commentsCount";



    //Himank Shah
    public void updateAssignmentAttempts(String name, int attempt) {

        Editor editor = getSessionSharedPreferences().edit();
        editor.putInt(name,
                attempt);
        editor.commit();
    }
    public int getAssignmentAttempts(String name) {

        SharedPreferences data = getSessionSharedPreferences();
        int attempts = data.getInt(name,0);
        return attempts;
    }





//    public void setCommentsCount(int count) {
//
//        Editor editor = getSessionSharedPreferences().edit();
//        editor.putInt(COMMENTS_COUNT, count);
//        editor.commit();
//    }
//
//
//    public int getCommentsCount() {
//        return getSessionSharedPreferences().getInt(COMMENTS_COUNT, 0);
//    }
}
