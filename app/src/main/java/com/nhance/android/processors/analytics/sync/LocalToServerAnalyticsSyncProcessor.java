package com.nhance.android.processors.analytics.sync;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.db.models.analytics.QuestionAnalytics;
import com.nhance.android.db.models.analytics.TestAnalytics;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.managers.LocalManager;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.content.infos.TestExtendedInfo;
import com.nhance.android.processors.anaytics.ReSetAnalyticsProcessor;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.SQLDBUtil;
import com.nhance.android.utils.VedantuWebUtils;
import com.nhance.android.R;

public class LocalToServerAnalyticsSyncProcessor extends AbstractAnalyticsSyncProcessor {

    private final String         TAG = "LocalToServerAnalyticsSyncProcessor";
    private AnalyticsDataManager analyticsManager;
    public String                errorMsg;
    private List<TestAnalytics>  testAnalytics;
    Handler                      handler;

    public LocalToServerAnalyticsSyncProcessor(Context context, Content content,
            List<TestAnalytics> testAnalytics, ITaskProcessor<JSONObject> taskProcessor) {

        super(context, content, taskProcessor);
        this.handler = new Handler();
        this.testAnalytics = testAnalytics;
        this.analyticsManager = new AnalyticsDataManager(context);
        this.url = session.getApiUrl("syncTabletAnalytics");
        httpParams = new HashMap<String, Object>();
    }

    @Override
    protected JSONObject doInBackground(String... params) {

        if (testAnalytics == null) {
            Log.e(TAG, "no attempt found corresponding to entity[id:" + test.id + ",type:"
                    + test.type + "] for user[" + test.userId + "]");
            errorMsg = context.getString(R.string.error_not_attempted);
            return null;
        }

        for (TestAnalytics ta : testAnalytics) {
            test = contentManager.getContent(ta.entityId, ta.entityType, ta.userId, ta.orgKeyId);
            if (test == null) {
                Log.e(TAG, "no content found for analytics : " + ta);
                continue;
            }
            httpParams.clear();
            syncAnalytics(ta);
        }
        return null;
    }

    private JSONObject syncAnalytics(TestAnalytics testAnalytics) {

        JSONObject resJSON = null;
        synchronized ((testAnalytics.entityType + testAnalytics.entityId + testAnalytics.userId)
                .intern()) {

            List<String> qIds = ((TestExtendedInfo) test.toContentExtendedInfo()).__getAllQIds();

            // fetch test analytics and corresponding question analytics and sync them with server
            List<QuestionAnalytics> questionAnalytics = analyticsManager.getQuestionAnalytics(
                    test.orgKeyId, test.userId, test.id, test.type, qIds, null);

            addReqParams(testAnalytics, qIds);

            qIds.clear();

            for (QuestionAnalytics qAnalytics : questionAnalytics) {
                qIds.add(qAnalytics.id);
                addQuestionAttemptReqParams(qAnalytics);
            }

            try {
                resJSON = VedantuWebUtils.getJSONData(url, VedantuWebUtils.POST_REQ, httpParams);
                error = VedantuWebUtils.checkErrorMsg(resJSON, url);
                if (error) {
                    String errorCode = JSONUtils.getString(resJSON, VedantuWebUtils.KEY_ERROR_CODE);
                    // {MULTI_ATTEMPTS_NOT_ALLOWED, ALREADY_ATTEMPTED} if the user has already
                    // attempted
                    // the test online then undo the local test
                    // analytics by calling reset method and re-calculating the analytics by
                    // fetching
                    // new analytics from server
                    Log.e(TAG, errorCode);
                    if (errorCode.equals("MULTI_ATTEMPTS_NOT_ALLOWED")
                            || errorCode.equals("ALREADY_ATTEMPTED")) {
                        overrideLocalAnalytics();
                    }
                    return null;
                }
                resJSON = JSONUtils.getJSONObject(resJSON, VedantuWebUtils.KEY_RESULT);
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
                return null;
            }

            if (JSONUtils.getBoolean(resJSON, "processed")) {
                // TODO:
                // if there is any difference between analytics or answer of questions in server
                // and local, then server analytics should be used to override local analytics
                markAnalyticsSynced(testAnalytics, qIds);
            }
        }
        return resJSON;
    }

    @Override
    protected void onPostExecute(JSONObject result) {

        super.onPostExecute(result);
        if (taskProcessor != null) {
            taskProcessor.onTaskPostExecute(!error, null);
        }
        clear();
    }

    private String DOT = ".";

    private void addQuestionAttemptReqParams(QuestionAnalytics qA) {

        String indexPrefix = "qusAttemptReqs[" + (qA.qusNo - 1) + "]";
        httpParams.put(indexPrefix + DOT + ConstantGlobal.QID, qA.id);
        httpParams.put(indexPrefix + DOT + ConstantGlobal.TIME_TAKEN,(int) qA.timeTaken * 1000);

        if (qA.answerGiven != null) {
            SessionManager.addListParams(
                    Arrays.asList(StringUtils.split(qA.answerGiven, SQLDBUtil.SEPARATOR)),
                    indexPrefix + DOT + ConstantGlobal.ANSWER_GIVEN, httpParams);
        }
        // TODO: check how to facilitate matrixAnswer submitting
    }

    private void addReqParams(TestAnalytics testAnalytics, List<String> qIds) {

        httpParams.put(ConstantGlobal.ENTITY_ID, testAnalytics.entityId);
        httpParams.put(ConstantGlobal.ENTITY_TYPE, testAnalytics.entityType);
        SessionManager.addListParams(qIds, ConstantGlobal.QIDS, httpParams);
        session.addSessionParams(httpParams);
        httpParams.put(ConstantGlobal.START_TIME, Long.valueOf(testAnalytics.timeCreated));
        httpParams.put(ConstantGlobal.END_TIME, Long.valueOf(testAnalytics.endTime));
    }

    @Override
    public void clear() {

        super.clear();
        analyticsManager = null;
        url = null;
        errorMsg = null;
        DOT = null;
        testAnalytics = null;
        test = null;
    }

    private void markAnalyticsSynced(TestAnalytics testAnalytics, List<String> qIds) {

        testAnalytics.synced = true;
        analyticsManager.updateTestAnalytics(testAnalytics);
        ContentValues contentValues = new ContentValues();
        contentValues.put(ConstantGlobal.SYNCED, 1);

        analyticsManager.update(AnalyticsDataManager.TABLE_QUESTION_ANALYTICS, contentValues,
                " id in (" + LocalManager.joinString(qIds, "'") + ") AND entityId='"
                        + testAnalytics.entityId + "' AND entityType='" + testAnalytics.entityType
                        + "'", null);

    }

    private void overrideLocalAnalytics() {

        handler.post(new Runnable() {

            @Override
            public void run() {

                final ServerToLocalAnalyticsSyncProcessor sToLocalAnalyticsSyncProcessor = new ServerToLocalAnalyticsSyncProcessor(
                        context, test, new ReSetAnalyticsProcessor(context, test), true);
                sToLocalAnalyticsSyncProcessor.executeTask(false);

            }
        });

    }

}
