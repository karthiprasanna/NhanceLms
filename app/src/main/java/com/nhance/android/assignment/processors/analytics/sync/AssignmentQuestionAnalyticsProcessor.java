package com.nhance.android.assignment.processors.analytics.sync;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus;
import com.nhance.android.assignment.pojo.assignment.AssignmentMetadata;
import com.nhance.android.assignment.processors.analytics.AssignmentAbstractAnalyticsSeriesProcessor;
import com.nhance.android.assignment.processors.analytics.AssignmentTopicAnalyticsProcessor;
import com.nhance.android.assignment.processors.analytics.overall.AssignmentOverallCourseWiseLevelAnalyticsProcessor;
import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.db.models.analytics.QuestionAnalytics;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.db.models.entity.Question;

import java.util.List;

/**
 * Created by Himank Shah on 12/6/2016.
 */

public class AssignmentQuestionAnalyticsProcessor  extends AssignmentAbstractAnalyticsSeriesProcessor {

    private final String TAG = "AssignmentQuestionAnalyticsProcessor";
    private AnalyticsDataManager manager;
    private Content assignment;
    private String userId;

    public AssignmentQuestionAnalyticsProcessor(Context context, Content assignment,
                                                List<AssignmentMetadata> courseMetadata, String userId) {

        super(new AssignmentAnalyticsProcessor(context, assignment, userId), new AssignmentCourseAnalyticsProcessor(
                context, assignment, courseMetadata, userId), new AssignmentTopicAnalyticsProcessor(context,
                assignment, userId), new AssignmentOverallCourseWiseLevelAnalyticsProcessor(context, userId));
        this.assignment = assignment;
        this.userId = userId;
        manager = new AnalyticsDataManager(context);
    }

    @Override
    public void process(TakeAssignmentQuestionWithAnswerGiven questionAnsInfo,
                        AssignementAttemptStatus attemptStatus, Question question) {

        Log.d(TAG, "processing questin analytics");
        createOrUpdateQuestionAnalytics(questionAnsInfo, attemptStatus, userId);
        super.process(questionAnsInfo, attemptStatus, question);
    }

    private void createOrUpdateQuestionAnalytics(TakeAssignmentQuestionWithAnswerGiven questionAnsInfo,
                                                 AssignementAttemptStatus attemptStatus, String userId) {

        QuestionAnalytics qAnalytics = manager.getQuestionAnalytics(assignment.orgKeyId, userId, assignment.id,
                assignment.type, questionAnsInfo.qId);
        Log.v(TAG, "questionAnsInfo.answerGiven : " + questionAnsInfo.answerGiven);
        if (qAnalytics != null) {
            updateQuestionAnalyticsModel(questionAnsInfo, qAnalytics, attemptStatus);
            Log.d(TAG, "updating question analytics");
            if (attemptStatus == AssignementAttemptStatus.RESET) {
                Log.d(TAG, "removing question analytics : " + qAnalytics);
                int rsp = manager.removeQuestionAnalytics(qAnalytics);
                Log.d(TAG, "removed qA status: " + rsp);
            } else {
                qAnalytics.synced = questionAnsInfo.isSynced();
                manager.updateQuestionAnalytics(qAnalytics);
            }
        } else if (!TextUtils.isEmpty(questionAnsInfo.answerGiven)) {
            Log.d(TAG, "creating question analytics");
            manager.createQuestionAnalytics(assignment, questionAnsInfo, userId);
        }
    }

    private void updateQuestionAnalyticsModel(TakeAssignmentQuestionWithAnswerGiven question,
                                              QuestionAnalytics analytics, AssignementAttemptStatus attemptStatus) {

        if (attemptStatus == AssignementAttemptStatus.SAVED) {
            analytics.score += question.getScore();
        } else if (attemptStatus == AssignementAttemptStatus.RESET) {
            analytics.score -= question.getScore();
        }
        analytics.timeTaken += question.getTimeTaken();
    }
}
