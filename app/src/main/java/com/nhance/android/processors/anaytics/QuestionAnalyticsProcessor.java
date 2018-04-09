package com.nhance.android.processors.anaytics;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.db.models.analytics.QuestionAnalytics;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.db.models.entity.Question;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven.AttemptStatus;
import com.nhance.android.pojos.tests.TestMetadata;
import com.nhance.android.processors.anaytics.overall.TestOverallCourseWiseLevelAnalyticsProcessor;

public class QuestionAnalyticsProcessor extends AbstractAnalyticsSeriesProcessor {

    private final String         TAG = "QuestionAnalyticsProcessor";
    private AnalyticsDataManager manager;
    private Content              test;
    private String               userId;

    public QuestionAnalyticsProcessor(Context context, Content test,
            List<TestMetadata> courseMetadata, String userId) {

        super(new TestAnalyticsProcessor(context, test, userId), new TestCourseAnalyticsProcessor(
                context, test, courseMetadata, userId), new TestTopicAnalyticsProcessor(context,
                test, userId), new TestOverallCourseWiseLevelAnalyticsProcessor(context, userId));
        this.test = test;
        this.userId = userId;
        manager = new AnalyticsDataManager(context);
    }

    @Override
    public void process(TakeTestQuestionWithAnswerGiven questionAnsInfo,
            AttemptStatus attemptStatus, Question question) {

        Log.d(TAG, "processing questin analytics");
        createOrUpdateQuestionAnalytics(questionAnsInfo, attemptStatus, userId);
        super.process(questionAnsInfo, attemptStatus, question);
    }

    private void createOrUpdateQuestionAnalytics(TakeTestQuestionWithAnswerGiven questionAnsInfo,
            AttemptStatus attemptStatus, String userId) {

        QuestionAnalytics qAnalytics = manager.getQuestionAnalytics(test.orgKeyId, userId, test.id,
                test.type, questionAnsInfo.qId);
        Log.v(TAG, "questionAnsInfo.answerGiven : " + questionAnsInfo.answerGiven);
        if (qAnalytics != null) {
            updateQuestionAnalyticsModel(questionAnsInfo, qAnalytics, attemptStatus);
            Log.d(TAG, "updating question analytics");
            if (attemptStatus == AttemptStatus.RESET) {
                Log.d(TAG, "removing question analytics : " + qAnalytics);
                int rsp = manager.removeQuestionAnalytics(qAnalytics);
                Log.d(TAG, "removed qA status: " + rsp);
            } else {
                qAnalytics.synced = questionAnsInfo.isSynced();
                manager.updateQuestionAnalytics(qAnalytics);
            }
        } else if (!TextUtils.isEmpty(questionAnsInfo.answerGiven)) {
            Log.d(TAG, "creating question analytics");
            manager.createQuestionAnalytics(test, questionAnsInfo, userId);
        }
    }

    private void updateQuestionAnalyticsModel(TakeTestQuestionWithAnswerGiven question,
            QuestionAnalytics analytics, AttemptStatus attemptStatus) {

        if (attemptStatus == AttemptStatus.SAVED) {
            analytics.score += question.getScore();
        } else if (attemptStatus == AttemptStatus.RESET) {
            analytics.score -= question.getScore();
        }
        analytics.timeTaken += question.getTimeTaken();
    }
}
