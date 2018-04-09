package com.nhance.android.receivers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.nhance.android.async.tasks.ActivityRecoderTask;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.async.tasks.ModuleEntryStatusSyncer;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.datamanagers.UserActivityDataManager;
import com.nhance.android.db.models.StudyHistory;
import com.nhance.android.db.models.analytics.TestAnalytics;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.processors.LocalToServerQuestionSyncProcessor;
import com.nhance.android.processors.analytics.sync.LocalToServerAnalyticsSyncProcessor;
import com.nhance.android.utils.ExecutorUtils;
import com.nhance.android.utils.VedantuWebUtils;

public class NetworkMonitor extends BroadcastReceiver implements ITaskProcessor<JSONObject> {

    private final int    DEFAULT_SIZE = 50;
    private final String TAG          = "NetworkMonitor";
    private Context      context;
    ContentDataManager   cd;
    Handler              handler;

    @Override
    public void onReceive(Context context, Intent intent) {

        boolean isOnline = VedantuWebUtils.isOnline(context);
        Log.d(TAG, "isOnline: " + isOnline);
        handler = new Handler();
        SessionManager.setOnline(isOnline);
        if (SessionManager.isOnline()) {
            this.context = context;
            cd = new ContentDataManager(context);
            Log.i(TAG, "starting syncer thread");
            handler.post(new SyncerThread());
            // ExecutorUtils.executor.execute(new SyncerThread());
        }
    }

    private void syncStudyHistory() {

        final SessionManager sm = SessionManager.getInstance(context);
        final UserActivityDataManager activitymanager = new UserActivityDataManager(context);

        List<StudyHistory> studyHistory = activitymanager.getUnSyncStudyHistory(sm
                .getSessionStringValue(ConstantGlobal.USER_ID));
        for (StudyHistory sh : studyHistory) {
            Content content = cd.getContent(sh.contentId);
            if (content == null) {
                continue;
            }
            Map<String, Object> httpParams = new HashMap<String, Object>();
            sm.addSessionParams(httpParams);
            httpParams.put("page", content.type);
            httpParams.put("userAction", "OPEN");
            httpParams.put("entity.type", content.type);
            httpParams.put("entity.id", content.id);
            httpParams.put("activityTime", Long.parseLong(sh.timeCreated));
            new ActivityRecoderTask(httpParams, context,
                    sm.getSessionStringValue(ConstantGlobal.CMDS_URL)).recordActivity(sh._id);

            Log.e("recordActivity","recordActivity"+httpParams);


        }
    }

    private class SyncerThread implements Runnable {

        @Override
        public void run() {

            if (context == null) {
                return;
            }
            Log.d(TAG, "syncing study history/user activity");
            final SessionManager sm = SessionManager.getInstance(context);

            if (StringUtils.isEmpty(sm.getSessionStringValue(ConstantGlobal.USER_ID))) {
                Log.w(TAG, "no active login");
                return;
            }
            if (sm.getSessionBooleanValue("autoLogin")) {
                Log.w(TAG, "default user active session, hence not syning");
                return;
            }
            try {
                syncAnalytics(context);
                syncStudyHistory();
                syncModule(context);
                syncQuestionStatus(context);
            } catch (Throwable e) {
                // TODO: handle exception
            }
        }
    }

    private void syncModule(Context context) {

        Log.d("fdfd", "begin_syncmodule");
        SessionManager session = SessionManager.getInstance(context);

        ModuleEntryStatusSyncer moduleEntryStatusSyncer;
        moduleEntryStatusSyncer = new ModuleEntryStatusSyncer(session, null, null,

        new ITaskProcessor<JSONObject>() {

            @Override
            public void onTaskStart(JSONObject result) {

            }

            @Override
            public void onTaskPostExecute(boolean success, JSONObject result) {

                Log.d("syncing taskes", "dsds");
            }
        });
        moduleEntryStatusSyncer.executeTask(false);
    }
    /**
     * this will sync local table questionStatus to server
     */

    private void syncQuestionStatus(Context context){
        if (context == null) {
            return;
        }
        SessionManager session = SessionManager.getInstance(context);
        LocalToServerQuestionSyncProcessor serverQuestionSyncer = new LocalToServerQuestionSyncProcessor(session, null);
        serverQuestionSyncer.executeTask(false);
    }


    /**
     * this will sync local tablet analytics to server
     */
    private void syncAnalytics(Context context) {

        if (context == null) {
            return;
        }

        AnalyticsDataManager analyticsManager = new AnalyticsDataManager(context);
        List<TestAnalytics> testAnalytics = analyticsManager.getUnSyncTestAnalytics(DEFAULT_SIZE);
        if (testAnalytics.isEmpty()) {
            return;
        }

        LocalToServerAnalyticsSyncProcessor serverAnalyticsSyncer = new LocalToServerAnalyticsSyncProcessor(
                context, null, testAnalytics, this);
        serverAnalyticsSyncer.executeTask(false);
    }

    @Override
    public void onTaskStart(JSONObject result) {

    }

    @Override
    public void onTaskPostExecute(boolean success, JSONObject result) {

        if (success) {
            Thread t = new Thread() {

                @Override
                public void run() {

                    super.run();
                    try {
                        long sleepTime = 5000;
                        Log.d(TAG, "sleeping for " + sleepTime + "ms");
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }

                    handler.post(new Runnable() {

                        @Override
                        public void run() {

                            syncAnalytics(context);

                        }
                    });

                }
            };
            ExecutorUtils.executor.execute(t);

        }
    }
}
