package com.nhance.android.async.tasks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ProgressBar;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.models.analytics.TestAnalytics;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.enums.EntityType;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.processors.analytics.sync.ServerToLocalAnalyticsSyncProcessor;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.VedantuWebUtils;

public class AnalyticsSyncer extends AbstractVedantuJSONAsyncTask {

    private final String               TAG = "AnalyticsSyncer";
    private String                     entityType;
    private ITaskProcessor<JSONObject> taskProcessor;
    public int                         syncCount;

    public AnalyticsSyncer(SessionManager session, ProgressBar progressUpdater, String entityType,
            ITaskProcessor<JSONObject> taskProcessor) {

        super(session, progressUpdater, new HashMap<String, Object>());
        this.url = session.getApiUrl("getAttemptedEntities");
        this.entityType = entityType;
        this.taskProcessor = taskProcessor;
    }

    /**
     * params will be list of specific entityIds for which analytics need to be checked and fetched
     */
    @Override
    protected JSONObject doInBackground(String... params) {

        httpParams.put(ConstantGlobal.TYPE, entityType);
        if (params != null) {
            SessionManager.addListParams(Arrays.asList(params), ConstantGlobal.IDS, httpParams);
        }
        httpParams.put("attemptedAfter", session.getLatestAnalyticsTime());
        session.addSessionParams(httpParams);
        JSONObject res = null;
        try {
            res = VedantuWebUtils.getJSONData(url, VedantuWebUtils.POST_REQ, httpParams);

            Log.d("FetchingAttemptedEntities","FetchingAttemptedEntities"+httpParams);
            Log.d("resFetchingAttemptedEntities","resFetchingAttemptedEntities"+res);


        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            error = true;
            return null;
        }
        error = VedantuWebUtils.checkErrorMsg(res, url);
        if (isCancelled()) {
            return null;
        }
        // if there is no error, the check what all entities the user has attempted online, and
        // filter out which he/she has already attempted on the tablet, as the analytics will be
        // synced
        // automatically for these entities and fetch analytics for rest of entities

        res = JSONUtils.getJSONObject(res, VedantuWebUtils.KEY_RESULT);
        if (taskProcessor != null) {
            taskProcessor.onTaskStart(res);
        }
        return processResponse(res);
    }

    @Override
    protected void onPostExecute(JSONObject result) {

        super.onPostExecute(result);

        if (taskProcessor != null) {
            taskProcessor.onTaskPostExecute(!error && result != null, result);
        }
        clear();
    }

    @Override
    protected void onCancelled() {

        super.onCancelled();
        Log.e(TAG, "task canclled by user");
    }

    @Override
    public void clear() {

        super.clear();
        taskProcessor = null;
        url = null;
        entityType = null;
    }

    private JSONObject processResponse(JSONObject res) {

        int totalHits = JSONUtils.getInt(res, "totalHits");
        Log.v(TAG, "totalHits : " + totalHits);
        JSONArray list = JSONUtils.getJSONArray(res, "list");
        List<String> entityIds = new ArrayList<String>();
        for (int i = 0; i < list.length(); i++) {
            try {
                JSONObject entity = list.getJSONObject(i);
                Log.v(TAG, "entity : " + entity);
                String id = JSONUtils.getString(entity, ConstantGlobal.ID);
                if (!TextUtils.isEmpty(id)) {
                    entityIds.add(id);
                }
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        // now filter out already attempt entites locally
        AnalyticsDataManager analyticsManager = new AnalyticsDataManager(session.getContext());
        List<TestAnalytics> attemptedTests = analyticsManager.getAllTestAnalytics(
                session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID),
                session.getSessionStringValue(ConstantGlobal.USER_ID), entityIds,
                EntityType.TEST.name(), new String[] { ConstantGlobal._ID,
                        ConstantGlobal.ENTITY_ID, ConstantGlobal.ENTITY_TYPE }, false);
        for (TestAnalytics ta : attemptedTests) {
            entityIds.remove(ta.entityId);
        }

        Log.v(TAG, "final entityIds for which analytics need to be fetched : " + entityIds);
        final ContentDataManager contentManager = new ContentDataManager(session.getContext());

        for (final String entityId : entityIds) {
            final Content content = contentManager.getContent(entityId, entityType,
                    session.getSessionStringValue(ConstantGlobal.USER_ID),
                    session.getSessionIntValue(ConstantGlobal.ORG_KEY_ID));
            if (content == null) {
                continue;
            }
            try {
            ServerToLocalAnalyticsSyncProcessor analyticSyncProcessor = new ServerToLocalAnalyticsSyncProcessor(
                    session.getContext(), content, new ITaskProcessor<JSONObject>() {

                        @Override
                        public void onTaskStart(JSONObject result) {

                        }

                        @Override
                        public void onTaskPostExecute(boolean success, JSONObject result) {

                            if (success) {
                                incSyncCount();
                                ContentValues values = new ContentValues();
                                values.put(ConstantGlobal.DOWNLOADED, 1);
                                contentManager.updateContent(content._id, values);
                            }
                        }
                    }, true);

            if (isCancelled()) {
                Log.e(TAG, "task canclled by user");
                analyticSyncProcessor.cancel(true);
                return res;
            }
            analyticSyncProcessor.executeTask(false);
            try {
                Log.v(TAG, Thread.currentThread().getName()
                        + " waiting for completion of analytics download for entityId: " + entityId);
                //analyticSyncProcessor.get();
            } catch (Exception e) {
                Log.e(TAG, "entity[id:" + entityId + ",type:" + entityType + "] " + e.getMessage(),
                        e);
            }

            if (isCancelled()) {
                Log.e(TAG, "task canclled by user");
                analyticSyncProcessor.cancel(true);
                return res;
            }
            Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        entityIds.clear();
        return res;
    }

    private synchronized void incSyncCount() {

        syncCount++;
    }
}
