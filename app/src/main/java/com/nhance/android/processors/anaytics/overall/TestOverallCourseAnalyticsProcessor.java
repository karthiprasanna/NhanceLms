package com.nhance.android.processors.anaytics.overall;

import android.content.Context;
import android.util.Log;

import com.nhance.android.db.models.analytics.TestBoardAnalytics;
import com.nhance.android.db.models.entity.BoardModel;
import com.nhance.android.db.models.entity.Question;
import com.nhance.android.enums.EntityType;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven.AttemptStatus;

public class TestOverallCourseAnalyticsProcessor extends AbstractOverallAnalyticsProcessor {

    public BoardModel courseModel;
    int               orgKeyId;

    public TestOverallCourseAnalyticsProcessor(Context context, String userId, int orgKeyId) {

        super(context, userId);
        this.orgKeyId = orgKeyId;

    }

    @Override
    public void process(TakeTestQuestionWithAnswerGiven questionAnsInfo,
            AttemptStatus attemptStatus, Question question) {

        if (courseModel == null) {
            return;
        }
        TestBoardAnalytics testCourseAnalytics = manager.getTestSingleBoardAnalytics(orgKeyId,
                userId, entityId, EntityType.TEST.name(), courseModel.id, courseModel.type, null);
        if (testCourseAnalytics == null) {
            if (attemptStatus != AttemptStatus.SAVED) {
                return;
            }
            testCourseAnalytics = new TestBoardAnalytics(orgKeyId, userId,
                    questionAnsInfo.getScore(), questionAnsInfo.getTimeTaken(), entityId,
                    EntityType.TEST.name(), questionAnsInfo.getMaxMarks(), 1, 1,
                    questionAnsInfo.correct ? 1 : 0, courseModel.id, courseModel.type,
                    courseModel.name, null);
            try {
                manager.createTestBoardAnalytics(testCourseAnalytics);
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return;
        }
        Log.d("TestOverallCourseAnalyticsProcessor", "updating test overallcourse analytics");
        updateAbstractAnalyticsModel(questionAnsInfo, testCourseAnalytics, attemptStatus);
        try {
            manager.updateTestBoardAnalytics(testCourseAnalytics);
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
