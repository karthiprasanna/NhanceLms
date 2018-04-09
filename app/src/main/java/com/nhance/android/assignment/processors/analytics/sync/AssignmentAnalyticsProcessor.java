package com.nhance.android.assignment.processors.analytics.sync;

import android.content.Context;
import android.util.Log;

import com.nhance.android.assignment.db.models.analytics.AssignmentAnalytics;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus;
import com.nhance.android.assignment.pojo.content.infos.AssignmentExtendedInfo;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.db.models.entity.Question;

/**
 * Created by Himank Shah on 12/6/2016.
 */

public class AssignmentAnalyticsProcessor extends AssignmentAbstractAnalyticsProcessor {

    private static final String TAG = "AssignmentAnalyticsProcessor";

    public AssignmentAnalyticsProcessor(Context context, Content assignment, String userId) {

        super(context, assignment, userId);
        manager.createAssignmentAnalytics(assignment, (AssignmentExtendedInfo) assignment.toContentExtendedInfo(), userId);
    }

    @Override
    public void process(TakeAssignmentQuestionWithAnswerGiven questionAnsInfo,
                        AssignementAttemptStatus attemptStatus, Question question) {

        AssignmentAnalytics assignmentAnalytics = manager.getAssignmentAnalytics(assignment.orgKeyId, userId, assignment.id,
                assignment.type);
        if (assignmentAnalytics != null) {
            updateAbstractAnalyticsModel(questionAnsInfo, assignmentAnalytics, attemptStatus);
            Log.d(TAG, "updated assignment analytics: " + assignmentAnalytics);
            manager.updateAssignmentAnalytics(assignmentAnalytics);
        } else {
            Log.e(TAG, "error on updating assignment analytics: " + assignmentAnalytics + ", update: "
                    + attemptStatus);
        }
    }
}
