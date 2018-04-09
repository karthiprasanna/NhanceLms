package com.nhance.android.processors.anaytics.overall;

import android.content.Context;
import android.util.Log;

import com.nhance.android.db.models.analytics.TestAnalytics;
import com.nhance.android.db.models.entity.Question;
import com.nhance.android.enums.EntityType;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven.AttemptStatus;

public class TestOverallAnalyticsProcessor extends AbstractOverallAnalyticsProcessor {

    public TestOverallAnalyticsProcessor(Context context, String userId) {

        super(context, userId);
    }

    @Override
    public void process(TakeTestQuestionWithAnswerGiven questionAnsInfo,
            AttemptStatus attemptStatus, Question question) {

        TestAnalytics testAnalytics = manager.getTestAnalytics(question.orgKeyId, userId, entityId,
                EntityType.TEST.name());
        if (testAnalytics != null) {
            updateAbstractAnalyticsModel(questionAnsInfo, testAnalytics, attemptStatus);
            Log.d("TestOverallAnalyticsProcessor", "updated overalltest analytics: "
                    + testAnalytics);
            manager.updateTestAnalytics(testAnalytics);
        } else {
            if (attemptStatus != AttemptStatus.SAVED) {
                return;
            }
            testAnalytics = new TestAnalytics(question.orgKeyId, userId,
                    questionAnsInfo.getScore(), questionAnsInfo.getTimeTaken(), entityId,
                    EntityType.TEST.name(), questionAnsInfo.getMaxMarks(), 1, 1,
                    questionAnsInfo.correct ? 1 : 0, 0);
            manager.createTestAnalytics(testAnalytics);
        }
    }

}
