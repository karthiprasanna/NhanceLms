package com.nhance.android.login.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;

import com.google.analytics.tracking.android.Fields;
import com.nhance.android.activities.AppLandingPageActivity;
import com.nhance.android.async.tasks.ActivityRecoderTask;
import com.nhance.android.async.tasks.LibraryLoader;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.UserDataManager;
import com.nhance.android.db.models.Organization;
import com.nhance.android.db.models.UserOrgProfile;
import com.nhance.android.db.models.entity.User;
import com.nhance.android.interfaces.ILoginPageListner;
import com.nhance.android.managers.LibraryManager;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.managers.VApp;
import com.nhance.android.pojos.OrgMemberInfo;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.VedantuEncrypter;

public class LoginUtils {

    private static final String TAG = "LoginUtils";

    public static void syncLibrary(final Context context) {

        if (!SessionManager.isOnline()) {
            startLandingPageActivity(context);
            return;
        }

        // sync library data
        OrgMemberInfo orgMemberInfo = SessionManager.getInstance(context.getApplicationContext())
                .getOrgMemberInfo();
        final List<LibraryLoader> loaders = LibraryManager.getInstance(
                context.getApplicationContext()).fetchLibraryContentsFromCMDS(null, orgMemberInfo,
                null, true);
        Log.d(TAG, "libraries to be synced: " + loaders.size());
        startLandingPageActivity(context);

    }

    private static void startLandingPageActivity(Context context) {

        SessionManager.getInstance(context.getApplicationContext())
                .captureUserInfoInGACustomDimensions();
        // starting a new session when the user logs in
        Log.d(TAG, "New google analytics session started when the user logs in");
        VApp.getGaTracker().set(Fields.SESSION_CONTROL, "start");
        // Staring LandingPageActivity
        Intent intent = new Intent(context, AppLandingPageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("showUserGuide", true);
        context.startActivity(intent);
        if (context instanceof ILoginPageListner) {
            ((ILoginPageListner) context).finishActivity();
        }
    }

    public static void createUserSession(User user, UserOrgProfile userOrgProfile,
            Organization org, UserDataManager userDataManager, Context context) {

        Log.d(TAG, "creating login session username:" + user.username + ", orgId:" + org.orgId);
        Log.d(TAG, "user orgProfile: " + userOrgProfile);
        SessionManager.getInstance(context.getApplicationContext()).createLoginSession(org, user,
                userOrgProfile);
        user.lastLogin = String.valueOf(System.currentTimeMillis());
        userDataManager.updateUser(user);
        final Map<String, Object> httpParams = new HashMap<String, Object>();
        httpParams.put(ConstantGlobal.ORG_ID, org.orgId);
        httpParams.put(ConstantGlobal.MEMBER_ID, user.userId);
        SessionManager.getInstance(context.getApplicationContext()).addSessionParams(httpParams);
        new ActivityRecoderTask(httpParams, context, org.cmdsUrl).recordLogin();

    }

    public static JSONObject loginUser(Organization organization, JSONObject orgProfileJSON,
            String username, String password, boolean autoLogin, UserDataManager userDataManager,
            Context context) throws Exception {

        User user = null;
        UserOrgProfile userOrgProfile = null;

        // store the user credentials locally for verifying off-line authentication

        JSONObject orgProfileInfoJSON = JSONUtils.getJSONObject(orgProfileJSON, "info");
        String thumb = JSONUtils.getString(orgProfileInfoJSON, ConstantGlobal.THUMBNAIL);
        String id = JSONUtils.getString(orgProfileInfoJSON, ConstantGlobal.USER_ID);
        user = userDataManager.getUser(organization._id, id);
        userOrgProfile = userDataManager.getUserOrgProfile(id, organization.orgId);
        password = VedantuEncrypter.INSTANCE.md5(password);
        if (user == null) {
            user = new User(organization._id, JSONUtils.getString(orgProfileInfoJSON,
                    ConstantGlobal.FIRST_NAME), JSONUtils.getString(orgProfileInfoJSON,
                    ConstantGlobal.LAST_NAME), username, id, password, thumb, String.valueOf(System
                    .currentTimeMillis()));

            user.key = Base64.decode(JSONUtils.getString(orgProfileInfoJSON, ConstantGlobal.KEY),
                    Base64.DEFAULT);
            user.autoLogin = autoLogin;
            if (userOrgProfile == null) {
                userOrgProfile = new UserOrgProfile(organization._id, id, organization.orgId,
                        orgProfileJSON);
            } else {
                userOrgProfile.orgProfile = orgProfileJSON;
            }
            userDataManager.insertUser(user);
            userDataManager.insertUserOrgProfile(userOrgProfile);

        } else {
            user.firstName = JSONUtils.getString(orgProfileInfoJSON, ConstantGlobal.FIRST_NAME);
            user.lastName = JSONUtils.getString(orgProfileInfoJSON, ConstantGlobal.LAST_NAME);
            user.password = password;
            user.thumb = thumb;
            user.lastLogin = String.valueOf(System.currentTimeMillis());
            userDataManager.updateUser(user);
            userOrgProfile.orgProfile = orgProfileJSON;
            userDataManager.updateUserOrgProfile(userOrgProfile);
        }
        createUserSession(user, userOrgProfile, organization, userDataManager, context);

        return orgProfileJSON;
    }

}
