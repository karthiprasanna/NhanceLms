package com.nhance.android.processors.anaytics.overall;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.nhance.android.db.datamanagers.BoardDataManager;
import com.nhance.android.db.models.analytics.TestBoardAnalytics;
import com.nhance.android.db.models.entity.BoardModel;
import com.nhance.android.db.models.entity.Question;
import com.nhance.android.enums.BoardType;
import com.nhance.android.enums.EntityType;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven.AttemptStatus;
import com.nhance.android.utils.SQLDBUtil;

public class TestOverallTopicAnalyticsProcessor extends AbstractOverallAnalyticsProcessor {

    public TestOverallTopicAnalyticsProcessor(Context context, String userId) {

        super(context, userId);
    }

    @Override
    public void process(TakeTestQuestionWithAnswerGiven questionAnsInfo,
            AttemptStatus attemptStatus, Question question) {

        String courseId = null;
        List<BoardModel> boards = new BoardDataManager(context).getBoards(Arrays.asList(TextUtils
                .split(question.brdIds, SQLDBUtil.SEPARATOR)));
        for (BoardModel b : boards) {
            if (BoardType.COURSE.name().equals(b.type)) {
                courseId = b.id;
                break;
            }
        }

        if (TextUtils.isEmpty(courseId)) {
            return;
        }

        for (BoardModel b : boards) {
            if (BoardType.COURSE.name().equals(b.type)) {
                // as course analytics has already been added
                continue;
            }
            TestBoardAnalytics testTopicAnalytics = manager.getTestSingleBoardAnalytics(
                    question.orgKeyId, userId, entityId, EntityType.TEST.name(), b.id, b.type,
                    courseId);
            if (testTopicAnalytics == null) {
                if (attemptStatus != AttemptStatus.SAVED) {
                    return;
                }
                testTopicAnalytics = new TestBoardAnalytics(questionAnsInfo.orgKeyId, userId,
                        questionAnsInfo.getScore(), questionAnsInfo.getTimeTaken(), entityId,
                        EntityType.TEST.name(), questionAnsInfo.getMaxMarks(), 1, 1,
                        questionAnsInfo.correct ? 1 : 0, b.id, b.type, b.name, courseId);
                try {
                    manager.createTestBoardAnalytics(testTopicAnalytics);
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return;
            }
            Log.d("TestOverallTopicAnalyticsProcessor", "updating test overalltopic analytics");
            updateAbstractAnalyticsModel(questionAnsInfo, testTopicAnalytics, attemptStatus);
            try {
                manager.updateTestBoardAnalytics(testTopicAnalytics);
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
