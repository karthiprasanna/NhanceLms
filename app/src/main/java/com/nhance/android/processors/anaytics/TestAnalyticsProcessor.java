package com.nhance.android.processors.anaytics;

import android.content.Context;
import android.util.Log;

import com.nhance.android.db.models.analytics.TestAnalytics;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.db.models.entity.Question;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven.AttemptStatus;
import com.nhance.android.pojos.content.infos.TestExtendedInfo;

public class TestAnalyticsProcessor extends AbstractAnalyticsProcessor {

    private static final String TAG = "TestAnalyticsProcessor";

    public TestAnalyticsProcessor(Context context, Content test, String userId) {

        super(context, test, userId);
        manager.createTestAnalytics(test, (TestExtendedInfo) test.toContentExtendedInfo(), userId);
    }

    @Override
    public void process(TakeTestQuestionWithAnswerGiven questionAnsInfo,
            AttemptStatus attemptStatus, Question question) {

        TestAnalytics testAnalytics = manager.getTestAnalytics(test.orgKeyId, userId, test.id,
                test.type);
        if (testAnalytics != null) {
            updateAbstractAnalyticsModel(questionAnsInfo, testAnalytics, attemptStatus);
            Log.d(TAG, "updated test analytics: " + testAnalytics);
            try {
                manager.updateTestAnalytics(testAnalytics);
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(TAG, "error on updating test analytics: " + testAnalytics + ", update: "
                    + attemptStatus);
        }
    }

}
