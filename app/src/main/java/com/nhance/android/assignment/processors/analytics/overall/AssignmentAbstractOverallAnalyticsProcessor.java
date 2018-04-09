package com.nhance.android.assignment.processors.analytics.overall;

import android.content.Context;

import com.nhance.android.assignment.db.models.analytics.AbstractAssignmentAnalytics;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus;
import com.nhance.android.assignment.processors.analytics.sync.AssignmentIAnalyticsProcessor;
import com.nhance.android.db.datamanagers.AnalyticsDataManager;

/**
 * Created by Himank Shah on 12/6/2016.
 */

public abstract class AssignmentAbstractOverallAnalyticsProcessor implements AssignmentIAnalyticsProcessor {

    protected final String entityId = AnalyticsDataManager.ASSIGNMENT_ID_OVERALL_SYSTEM;
    protected String userId;
    protected Context context;
    protected AnalyticsDataManager manager;

    public AssignmentAbstractOverallAnalyticsProcessor(Context context, String userId) {

        this.userId = userId;
        this.context = context;
        if (this.manager == null) {
            this.manager = new AnalyticsDataManager(context);
        }
    }

    protected void updateAbstractAnalyticsModel(TakeAssignmentQuestionWithAnswerGiven questionInfo,
                                                AbstractAssignmentAnalytics analytics, AssignementAttemptStatus attemptStatus) {

        if (attemptStatus == AssignementAttemptStatus.SAVED) {
            analytics.attempted++;
            analytics.qusCount++;
            analytics.totalMarks += questionInfo.getMaxMarks();
            if (questionInfo.correct) {
                analytics.correct++;
            }
            analytics.score += questionInfo.getScore();
        } else if (attemptStatus == AssignementAttemptStatus.RESET) {
            analytics.attempted--;
            analytics.qusCount--;
            analytics.totalMarks -= questionInfo.getMaxMarks();
            if (questionInfo.correct) {
                analytics.correct--;
            }
            analytics.score -= questionInfo.getScore();
        }
        analytics.timeTaken += questionInfo.getTimeTaken();
    }

}
