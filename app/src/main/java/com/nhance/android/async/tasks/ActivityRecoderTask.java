package com.nhance.android.async.tasks;

import java.util.Map;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.UserActivityDataManager;
import com.nhance.android.factory.CMDSUrlFactory;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.VedantuWebUtils;

public class ActivityRecoderTask extends AsyncTask<String, Void, JSONObject> {

    private final String TAG = "ActivityRecoderTask";
    Map<String, Object>  httpParams;
    Context              context;
    String               cmdsHost;
    int                  localActivityId;

    public ActivityRecoderTask(Map<String, Object> httpParams, Context context) {

        this(httpParams, context, null);
    }

    public ActivityRecoderTask(Map<String, Object> httpParams, Context context, String cmdsHost) {

        super();
        this.httpParams = httpParams;
        this.context = context;
        this.cmdsHost = cmdsHost == null ? SessionManager.getInstance(context)
                .getSessionStringValue(ConstantGlobal.CMDS_URL) : cmdsHost;
        this.localActivityId = -1;
    }

    public void recordLogin() {

        executeTask(true, CMDSUrlFactory.INSTANCE.getCMDSUrl(cmdsHost, "recordLogin"));
    }

    public void recordLogout() {

        executeTask(true, CMDSUrlFactory.INSTANCE.getCMDSUrl(cmdsHost, "recordLogout"));
    }

    public void recordActivity(int localActivityId) {

        this.localActivityId = localActivityId;
        executeTask(true, CMDSUrlFactory.INSTANCE.getCMDSUrl(cmdsHost, "recordActivity"));
    }

    @SuppressLint("NewApi")
    public ActivityRecoderTask executeTask(boolean singleThread, String... params) {

        if (singleThread || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            Log.d("ExecutorUtils", "calling execute");
            execute(params);
        } else {
            Log.d("ExecutorUtils", "calling executeOnExecutor");
            executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        }
        return this;
    }

    @Override
    protected JSONObject doInBackground(String... params) {

        try {
            if (!SessionManager.isOnline()) {
                return null;
            }

            if (SessionManager.getInstance(context).getSessionBooleanValue("autoLogin")) {
                Log.w(TAG, "default user active session, hence recordActivity is disabled");
                return null;
            }

            JSONObject json = VedantuWebUtils.getJSONData(params[0], VedantuWebUtils.POST_REQ,
                    httpParams);
            JSONObject resultJSON = json == null ? null : JSONUtils.getJSONObject(json, "result");
            if (json != null && resultJSON != null && resultJSON.length() > 0
                    && JSONUtils.getBoolean(resultJSON, "recorded")) {
                if (localActivityId != -1) {
                    ContentValues values = new ContentValues();
                    values.put(ConstantGlobal.SYNCED, 1);
                    new UserActivityDataManager(context)
                            .updateStudyHistory(localActivityId, values);
                }
            }
            return json;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }
}
