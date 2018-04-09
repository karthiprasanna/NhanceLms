package com.nhance.android.processors.analytics.sync;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.db.models.analytics.TestAnalytics;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.downloader.DownloadInfo.DownloadState;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven.AttemptStatus;
import com.nhance.android.pojos.content.infos.TestExtendedInfo;
import com.nhance.android.pojos.tests.Marks;
import com.nhance.android.pojos.tests.TestMetadata;
import com.nhance.android.processors.anaytics.QuestionAnalyticsProcessor;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.SQLDBUtil;
import com.nhance.android.utils.VedantuWebUtils;
import com.nhance.android.R;

public class ServerToLocalAnalyticsSyncProcessor extends AbstractAnalyticsSyncProcessor {

    private final String       TAG = "ServerToLocalAnalyticsSyncProcessor";
    private String             url;
    public String              errorMsg;
    TestExtendedInfo           testInfo;
    QuestionAnalyticsProcessor qAnalyticsProcessor;
    Map<String, Marks>         marksMap;
    private boolean            backgroundMode;
    private boolean            downloadQuestions;

    public ServerToLocalAnalyticsSyncProcessor(Context context, Content content,
            ITaskProcessor<JSONObject> taskProcessor, boolean backgroundMode) {

        super(context, content, taskProcessor);
        this.downloadQuestions = !content.downloaded;
        this.backgroundMode = backgroundMode;
        this.url = session.getApiUrl("getEntityQuestionsAttemptStat");
        this.testInfo = (TestExtendedInfo) content.toContentExtendedInfo();
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
        httpParams.put("entity.id", test.id);
        httpParams.put("entity.type", test.type);
        httpParams.put("downloadQuestions", String.valueOf(downloadQuestions));
        session.addSessionParams(httpParams);

        if (dInfo.state == DownloadState.TOSTART || dInfo.state == DownloadState.STARTED) {
            dInfo.state = DownloadState.STARTED;
            dInfo.lastStarted = System.currentTimeMillis();
        }

        try {
            jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.POST_REQ, httpParams);

            Log.d("getEntityQuestionsAttemptStat","getEntityQuestionsAttemptStat"+jsonRes);
            Log.d("FetchinggetEntityQuestionsAttemptStat","FetchinggetEntityQuestionsAttemptStat"+httpParams);


        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }

        if (isCancelled()) {
            Log.e(TAG, "task cancled by user");
            return null;
        }

        if (jsonRes == null) {
            errorMsg = context.getString(R.string.error_not_downloaded);;
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

        synchronized ((test.orgKeyId + test.userId + test.id + test.type).intern()) {
            AnalyticsDataManager ad = new AnalyticsDataManager(context);
            TestAnalytics testAnalytics = ad.getTestAnalytics(test.orgKeyId, test.userId, test.id,
                    test.type);

            if (testAnalytics != null && testAnalytics.synced) {
                Log.e(TAG, " testAnalytics already synced : " + testAnalytics);
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

        this.qAnalyticsProcessor = new QuestionAnalyticsProcessor(context, test, testInfo.metadata,
                test.userId);

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

        TestAnalytics testAnalytics = ad.getTestAnalytics(test.orgKeyId, test.userId, test.id,
                test.type);

        if (testAnalytics == null || testAnalytics.synced) {
            Log.e(TAG, " null testAnalytics or already synced : " + testAnalytics);
            return;
        }

        Map<String, TakeTestQuestionWithAnswerGiven> ansInfoMap = ad
                .getQuestionWithAnswerAndQuestionMap(test.orgKeyId, test.userId,
                        testInfo.__getAllQIds());
        for (int i = 0; i < list.length(); i++) {
            try {
                JSONObject json = list.getJSONObject(i);
                String qId = JSONUtils.getString(json, ConstantGlobal.QID);
                updateEntityLocalAnalytics(json, ansInfoMap.get(qId));
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }

        testAnalytics = ad.getTestAnalytics(test.orgKeyId, test.userId, test.id, test.type);
        long startTime = JSONUtils.getLong(jsonRes, ConstantGlobal.START_TIME);
        testAnalytics.timeCreated = String.valueOf(startTime);
        long endTime = JSONUtils.getLong(jsonRes, ConstantGlobal.END_TIME);
        testAnalytics.endTime = String.valueOf(endTime);
        testAnalytics.timeTaken = (int) (endTime - startTime) / 1000;
        testAnalytics.synced = true;
        ad.updateTestAnalytics(testAnalytics);
        session.updateLatestAnalyticsTime(endTime);

    }

    private void updateEntityLocalAnalytics(JSONObject json,
            TakeTestQuestionWithAnswerGiven questionAnsInfo) {

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
        questionAnsInfo.setStatus(attemptStatus.equalsIgnoreCase("ATTEMPTED") ? AttemptStatus.SAVED
                : AttemptStatus.SKIP);
        int maxMark = marksMap.get(questionAnsInfo.qId) == null ? 0 : marksMap
                .get(questionAnsInfo.qId).positive;
        questionAnsInfo.setMaxMarks(maxMark);
        questionAnsInfo.setScore(JSONUtils.getInt(json, ConstantGlobal.SCORE));
        questionAnsInfo.setQusNo(JSONUtils.getInt(json, "qusNo"));
        questionAnsInfo.setTimeTaken(JSONUtils.getInt(json, ConstantGlobal.TIME_TAKEN) / 1000);
        questionAnsInfo.setSynced(true);
        qAnalyticsProcessor.process(questionAnsInfo, questionAnsInfo.getStatus(),
                questionAnsInfo.getQuestion(test.userId, contentManager));
    }

    private void intiMarksMap() {

        marksMap = new HashMap<String, Marks>();
        for (TestMetadata mDMetadata : testInfo.metadata) {
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
        testInfo = null;
        qAnalyticsProcessor = null;
        taskProcessor = null;
        errorMsg = null;
        url = null;
    }

}
