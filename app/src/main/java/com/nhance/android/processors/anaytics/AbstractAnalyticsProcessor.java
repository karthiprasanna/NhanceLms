package com.nhance.android.processors.anaytics;

import android.content.Context;

import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.db.models.AbstractTestAnalytics;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven.AttemptStatus;

public abstract class AbstractAnalyticsProcessor implements IAnalyticsProcessor {

    protected Content              test;
    protected Context              context;
    protected AnalyticsDataManager manager;
    protected String               userId;

    public AbstractAnalyticsProcessor(Context context, Content test, String userId) {

        this.context = context;
        this.test = test;
        this.userId = userId;
        if (manager == null) {
            manager = new AnalyticsDataManager(context);
        }
    }

    protected void updateAbstractAnalyticsModel(TakeTestQuestionWithAnswerGiven questionAnsInfo,
            AbstractTestAnalytics analytics, AttemptStatus attemptStatus) {

        if (attemptStatus == AttemptStatus.SAVED) {
            analytics.attempted++;
            if (questionAnsInfo.correct) {
                analytics.correct++;
            }
            analytics.score += questionAnsInfo.getScore();
        } else if (attemptStatus == AttemptStatus.RESET) {
            analytics.attempted--;
            if (questionAnsInfo.correct) {
                analytics.correct--;
            }
            analytics.score -= questionAnsInfo.getScore();

        }
        analytics.timeTaken += questionAnsInfo.getTimeTaken();
    }
}
