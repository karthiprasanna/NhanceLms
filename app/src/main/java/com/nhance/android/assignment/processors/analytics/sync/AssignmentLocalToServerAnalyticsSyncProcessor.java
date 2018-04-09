package com.nhance.android.assignment.processors.analytics.sync;

import android.content.ContentValues;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.nhance.android.R;
import com.nhance.android.assignment.db.models.analytics.AssignmentAnalytics;
import com.nhance.android.assignment.managers.AssignmentLocalManager;
import com.nhance.android.assignment.pojo.content.infos.AssignmentExtendedInfo;
import com.nhance.android.assignment.processors.analytics.AssignmentReSetAnalyticsProcessor;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.db.models.analytics.QuestionAnalytics;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.SQLDBUtil;
import com.nhance.android.utils.VedantuWebUtils;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Himank Shah on 12/6/2016.
 */

public class AssignmentLocalToServerAnalyticsSyncProcessor extends AssignmentAbstractAnalyticsSyncProcessor {

    private final String TAG = "AssignmentLocalToServerAnalyticsSyncProcessor";
    private AnalyticsDataManager analyticsManager;
    public String errorMsg;
    private List<AssignmentAnalytics> assignmentAnalytics;
    Handler handler;

    public AssignmentLocalToServerAnalyticsSyncProcessor(Context context, Content content,
                                                         List<AssignmentAnalytics> assignmentAnalytics, ITaskProcessor<JSONObject> taskProcessor) {

        super(context, content, taskProcessor);
        this.handler = new Handler();
        this.assignmentAnalytics = assignmentAnalytics;
        this.analyticsManager = new AnalyticsDataManager(context);
        this.url = session.getApiUrl("syncTabletAnalytics");
        httpParams = new HashMap<String, Object>();
    }

    @Override
    protected JSONObject doInBackground(String... params) {

        if (assignmentAnalytics == null) {
            Log.e(TAG, "no attempt found corresponding to entity[id:" + assignment.id + ",type:"
                    + assignment.type + "] for user[" + assignment.userId + "]");
            errorMsg = context.getString(R.string.error_assigment_not_attempted);
            return null;
        }

        for (AssignmentAnalytics ta : assignmentAnalytics) {
            assignment = contentManager.getContent(ta.entityId, ta.entityType, ta.userId, ta.orgKeyId);
            if (assignment == null) {
                Log.e(TAG, "no content found for analytics : " + ta);
                continue;
            }
            httpParams.clear();
            syncAnalytics(ta);
        }
        return null;
    }

    private JSONObject syncAnalytics(AssignmentAnalytics assignmentAnalytics) {

        JSONObject resJSON = null;
        synchronized ((assignmentAnalytics.entityType + assignmentAnalytics.entityId + assignmentAnalytics.userId)
                .intern()) {

            List<String> qIds = ((AssignmentExtendedInfo) assignment.toContentExtendedInfo()).__getAllQIds();

            // fetch assignment analytics and corresponding question analytics and sync them with server
            List<QuestionAnalytics> questionAnalytics = analyticsManager.getQuestionAnalytics(
                    assignment.orgKeyId, assignment.userId, assignment.id, assignment.type, qIds, null);

            addReqParams(assignmentAnalytics, qIds);

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
                    // the assignment online then undo the local assignment
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
                markAnalyticsSynced(assignmentAnalytics, qIds);
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
        httpParams.put(indexPrefix + DOT + ConstantGlobal.TIME_TAKEN, qA.timeTaken * 1000);

        if (qA.answerGiven != null) {
            SessionManager.addListParams(
                    Arrays.asList(StringUtils.split(qA.answerGiven, SQLDBUtil.SEPARATOR)),
                    indexPrefix + DOT + ConstantGlobal.ANSWER_GIVEN, httpParams);
        }
        // TODO: check how to facilitate matrixAnswer submitting
    }

    private void addReqParams(AssignmentAnalytics assignmentAnalytics, List<String> qIds) {

        httpParams.put(ConstantGlobal.ENTITY_ID, assignmentAnalytics.entityId);
        httpParams.put(ConstantGlobal.ENTITY_TYPE, assignmentAnalytics.entityType);
        SessionManager.addListParams(qIds, ConstantGlobal.QIDS, httpParams);
        session.addSessionParams(httpParams);
        httpParams.put(ConstantGlobal.START_TIME, Long.valueOf(assignmentAnalytics.timeCreated));
        httpParams.put(ConstantGlobal.END_TIME, Long.valueOf(assignmentAnalytics.endTime));
    }

    @Override
    public void clear() {

        super.clear();
        analyticsManager = null;
        url = null;
        errorMsg = null;
        DOT = null;
        assignmentAnalytics = null;
        assignment = null;
    }

    private void markAnalyticsSynced(AssignmentAnalytics assignmentAnalytics, List<String> qIds) {

        assignmentAnalytics.synced = true;
        analyticsManager.updateAssignmentAnalytics(assignmentAnalytics);
        ContentValues contentValues = new ContentValues();
        contentValues.put(ConstantGlobal.SYNCED, 1);

        analyticsManager.update(AnalyticsDataManager.TABLE_QUESTION_ANALYTICS, contentValues,
                " id in (" + AssignmentLocalManager.joinString(qIds, "'") + ") AND entityId='"
                        + assignmentAnalytics.entityId + "' AND entityType='" + assignmentAnalytics.entityType
                        + "'", null);

    }

    private void overrideLocalAnalytics() {

        handler.post(new Runnable() {

            @Override
            public void run() {

                final AssignmentServerToLocalAnalyticsSyncProcessor sToLocalAnalyticsSyncProcessor = new AssignmentServerToLocalAnalyticsSyncProcessor(
                        context, assignment, new AssignmentReSetAnalyticsProcessor(context, assignment), true);
                sToLocalAnalyticsSyncProcessor.executeTask(false);

            }
        });

    }

}
