package com.nhance.android.assignment.processors.analytics.sync;

import android.content.Context;

import com.nhance.android.assignment.db.models.analytics.AbstractAssignmentAnalytics;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus;
import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.db.models.entity.Content;

/**
 * Created by Himank Shah on 12/6/2016.
 */

public abstract class AssignmentAbstractAnalyticsProcessor implements AssignmentIAnalyticsProcessor {

    protected Content assignment;
    protected Context context;
    protected AnalyticsDataManager manager;
    protected String userId;

    public AssignmentAbstractAnalyticsProcessor(Context context, Content assignment, String userId) {

        this.context = context;
        this.assignment = assignment;
        this.userId = userId;
        if (manager == null) {
            manager = new AnalyticsDataManager(context);
        }
    }

    protected void updateAbstractAnalyticsModel(TakeAssignmentQuestionWithAnswerGiven questionAnsInfo,
                                                AbstractAssignmentAnalytics analytics, AssignementAttemptStatus attemptStatus) {

        if (attemptStatus == AssignementAttemptStatus.SAVED) {
            analytics.attempted++;
            if (questionAnsInfo.correct) {
                analytics.correct++;
            }
            analytics.score += questionAnsInfo.getScore();
        } else if (attemptStatus == AssignementAttemptStatus.RESET) {
            analytics.attempted--;
            if (questionAnsInfo.correct) {
                analytics.correct--;
            }
            analytics.score -= questionAnsInfo.getScore();

        }
        analytics.timeTaken += questionAnsInfo.getTimeTaken();
    }

}
