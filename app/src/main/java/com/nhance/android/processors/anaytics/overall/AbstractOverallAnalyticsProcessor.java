package com.nhance.android.processors.anaytics.overall;

import android.content.Context;

import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.db.models.AbstractTestAnalytics;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven.AttemptStatus;
import com.nhance.android.processors.anaytics.IAnalyticsProcessor;

public abstract class AbstractOverallAnalyticsProcessor implements IAnalyticsProcessor {

    protected final String         entityId = AnalyticsDataManager.TEST_ID_OVERALL_SYSTEM;
    protected String               userId;
    protected Context              context;
    protected AnalyticsDataManager manager;

    public AbstractOverallAnalyticsProcessor(Context context, String userId) {

        this.userId = userId;
        this.context = context;
        if (this.manager == null) {
            this.manager = new AnalyticsDataManager(context);
        }
    }

    protected void updateAbstractAnalyticsModel(TakeTestQuestionWithAnswerGiven questionInfo,
            AbstractTestAnalytics analytics, AttemptStatus attemptStatus) {

        if (attemptStatus == AttemptStatus.SAVED) {
            analytics.attempted++;
            analytics.qusCount++;
            analytics.totalMarks += questionInfo.getMaxMarks();
            if (questionInfo.correct) {
                analytics.correct++;
            }
            analytics.score += questionInfo.getScore();
        } else if (attemptStatus == AttemptStatus.RESET) {
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
