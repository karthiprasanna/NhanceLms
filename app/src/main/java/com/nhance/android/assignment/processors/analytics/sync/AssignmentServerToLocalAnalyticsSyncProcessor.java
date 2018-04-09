package com.nhance.android.assignment.processors.analytics.sync;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.nhance.android.R;
import com.nhance.android.assignment.db.models.analytics.AssignmentAnalytics;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus;
import com.nhance.android.assignment.pojo.assignment.AssignmentMetadata;
import com.nhance.android.assignment.pojo.content.infos.AssignmentExtendedInfo;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.downloader.DownloadInfo;
import com.nhance.android.pojos.tests.Marks;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.SQLDBUtil;
import com.nhance.android.utils.VedantuWebUtils;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Himank Shah on 12/6/2016.
 */

public class AssignmentServerToLocalAnalyticsSyncProcessor extends AssignmentAbstractAnalyticsSyncProcessor {

    private final String TAG = "AssignmentServerToLocalAnalyticsSyncProcessor";
    private String url;
    public String errorMsg;
    AssignmentExtendedInfo assignmentInfo;
    AssignmentQuestionAnalyticsProcessor qAnalyticsProcessor;
    Map<String, Marks> marksMap;
    private boolean backgroundMode;
    private boolean downloadQuestions;

    public AssignmentServerToLocalAnalyticsSyncProcessor(Context context, Content content,
                                                         ITaskProcessor<JSONObject> taskProcessor, boolean backgroundMode) {

        super(context, content, taskProcessor);
        this.downloadQuestions = !content.downloaded;
        this.backgroundMode = backgroundMode;

        this.url = session.getApiUrl("getUserEntityQuestionAttempts");
        this.assignmentInfo = (AssignmentExtendedInfo) content.toContentExtendedInfo();
        intiMarksMap();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {

        super.onProgressUpdate(values);
    }

    @Override
    protected JSONObject doInBackground(String... params) {

        JSONObject jsonRes = null;

        Map<String, Object> httpParams = new HashMap<String, Object>();
        httpParams.put("entity.id", session.getOrgMemberInfo().id);
        httpParams.put("entity.type", "ASSIGNMENT");
      httpParams.put("targetUserId",session.getOrgMemberInfo().userId);

        httpParams.put("downloadQuestions", String.valueOf(downloadQuestions));
        session.addSessionParams(httpParams);

        if (dInfo.state == DownloadInfo.DownloadState.TOSTART || dInfo.state == DownloadInfo.DownloadState.STARTED) {
            dInfo.state = DownloadInfo.DownloadState.STARTED;
            dInfo.lastStarted = System.currentTimeMillis();
        }

        try {
            jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.POST_REQ, httpParams);
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        if (isCancelled()) {
            Log.e(TAG, "task cancled by user");
            return null;
        }

        if (jsonRes == null) {
            errorMsg = context.getString(R.string.error_not_downloaded);
            ;
            return null;
        }

        String errorCode = null;
        if (!TextUtils.isEmpty(errorCode = JSONUtils.getString(jsonRes,
                VedantuWebUtils.KEY_ERROR_CODE))) {
            if (errorCode.equals("NOT_ATTEMPTED")) {
                errorMsg = context.getString(R.string.error_not_attempted);
            } else if (errorCode.equals("ATTEMPT_IN_PROGRESS")) {
                errorMsg = context.getString(R.string.error_attempt_in_progress);
            } else {
                errorMsg = errorCode;
            }
            Log.e(TAG, "errorMessage: " + JSONUtils.getString(jsonRes, "errorMessage"));
            return null;
        }

        synchronized ((assignment.orgKeyId + assignment.userId + assignment.id + assignment.type).intern()) {
            AnalyticsDataManager ad = new AnalyticsDataManager(context);
            AssignmentAnalytics assignmentAnalytics = ad.getAssignmentAnalytics(assignment.orgKeyId, assignment.userId, assignment.id,
                    assignment.type);

            if (assignmentAnalytics != null && assignmentAnalytics.synced) {
                Log.e(TAG, " assignmentAnalytics already synced : " + assignmentAnalytics);
                return null;
            }

            if (taskProcessor != null) {
                taskProcessor.onTaskStart(jsonRes);
            }

            updateEntityLocalAnalytics(ad, jsonRes);
        }

        return jsonRes;
    }

    @Override
    protected void onPostExecute(JSONObject result) {

        super.onPostExecute(result);
        try {
            if (!backgroundMode && !TextUtils.isEmpty(errorMsg)) {
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
                return;
            }
        } finally {
            if (taskProcessor != null) {
                taskProcessor.onTaskPostExecute(
                        !isCancelled() && result != null && TextUtils.isEmpty(errorMsg), result);
            }
        }
        clear();
    }

    @Override
    protected void onCancelled() {

        super.onCancelled();
        Log.e(TAG, "task canclled by user");
    }

    private void updateEntityLocalAnalytics(AnalyticsDataManager ad, JSONObject jsonRes) {

        jsonRes = JSONUtils.getJSONObject(jsonRes, VedantuWebUtils.KEY_RESULT);
        JSONArray list = JSONUtils.getJSONArray(jsonRes, "list");
        if (list == null || list.length() == 0) {
            Log.e(TAG, "non question attempt list present");
            return;
        }

        this.qAnalyticsProcessor = new AssignmentQuestionAnalyticsProcessor(context, assignment, assignmentInfo.metadata,
                assignment.userId);

        dInfo.total = list.length();
        updateDownloadHistory(false);
        if (downloadQuestions) {
            for (int i = 0; i < list.length(); i++) {
                try {
                    JSONObject json = JSONUtils.getJSONObject(list.getJSONObject(i),
                            ConstantGlobal.CONTENT);
                    dInfo.downloaded++;
                    updateQuestion(json, SyncType.ONLINE);
                    updateProgress();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        }

        AssignmentAnalytics assignmentAnalytics = ad.getAssignmentAnalytics(assignment.orgKeyId, assignment.userId, assignment.id,
                assignment.type);

        if (assignmentAnalytics == null || assignmentAnalytics.synced) {
            Log.e(TAG, " null assignmentAnalytics or already synced : " + assignmentAnalytics);
            return;
        }

        Map<String, TakeAssignmentQuestionWithAnswerGiven> ansInfoMap = ad
                .getAssignmentQuestionWithAnswerAndQuestionMap(assignment.orgKeyId, assignment.userId,
                        assignmentInfo.__getAllQIds());
        for (int i = 0; i < list.length(); i++) {
            try {
                JSONObject json = list.getJSONObject(i);
                String qId = JSONUtils.getString(json, ConstantGlobal.QID);
                updateEntityLocalAnalytics(json, ansInfoMap.get(qId));
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        assignmentAnalytics = ad.getAssignmentAnalytics(assignment.orgKeyId, assignment.userId, assignment.id, assignment.type);
        long startTime = JSONUtils.getLong(jsonRes, ConstantGlobal.START_TIME);
        assignmentAnalytics.timeCreated = String.valueOf(startTime);
        long endTime = JSONUtils.getLong(jsonRes, ConstantGlobal.END_TIME);
        assignmentAnalytics.endTime = String.valueOf(endTime);
        assignmentAnalytics.timeTaken = (int) (endTime - startTime) / 1000;
        assignmentAnalytics.synced = true;
        ad.updateAssignmentAnalytics(assignmentAnalytics);
        session.updateLatestAnalyticsTime(endTime);

    }

    private void updateEntityLocalAnalytics(JSONObject json,
                                            TakeAssignmentQuestionWithAnswerGiven questionAnsInfo) {

        if (questionAnsInfo == null) {
            return;
        }
        String answerGiven = StringUtils.join(JSONUtils.getList(json, ConstantGlobal.ANSWER_GIVEN),
                SQLDBUtil.SEPARATOR);
        questionAnsInfo.answerGiven = answerGiven;
        if (answerGiven == null) {
            questionAnsInfo.correctMatrixAnswer = JSONUtils
                    .getJSONObject(json, "matrixAnswerGiven").toString();
        }

        questionAnsInfo.correct = JSONUtils.getBoolean(json, "isCorrect");

        String attemptStatus = JSONUtils.getString(json, ConstantGlobal.STATUS);

        questionAnsInfo.setAnalysed(true);
        questionAnsInfo.setStatus(attemptStatus.equalsIgnoreCase("ATTEMPTED") ? AssignementAttemptStatus.SAVED
                : AssignementAttemptStatus.SKIP);
        int maxMark = marksMap.get(questionAnsInfo.qId) == null ? 0 : marksMap
                .get(questionAnsInfo.qId).positive;
        questionAnsInfo.setMaxMarks(maxMark);
        questionAnsInfo.setScore(JSONUtils.getInt(json, ConstantGlobal.SCORE));
        questionAnsInfo.setQusNo(JSONUtils.getInt(json, "qusNo"));
        questionAnsInfo.setTimeTaken(JSONUtils.getInt(json, ConstantGlobal.TIME_TAKEN) / 1000);
        questionAnsInfo.setSynced(true);
        qAnalyticsProcessor.process(questionAnsInfo, questionAnsInfo.getStatus(),
                questionAnsInfo.getQuestion(assignment.userId, contentManager));
    }

    private void intiMarksMap() {

        marksMap = new HashMap<String, Marks>();
        for (AssignmentMetadata mDMetadata : assignmentInfo.metadata) {
            mDMetadata.finishEditing();
            marksMap.putAll(mDMetadata.marks);
        }
    }

    @Override
    public void clear() {

        super.clear();
        if (marksMap != null) {
            marksMap.clear();
        }
        marksMap = null;
        assignmentInfo = null;
        qAnalyticsProcessor = null;
        taskProcessor = null;
        errorMsg = null;
        url = null;
    }

}
