package com.nhance.android.processors.anaytics;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.nhance.android.db.datamanagers.BoardDataManager;
import com.nhance.android.db.models.analytics.TestBoardAnalytics;
import com.nhance.android.db.models.entity.BoardModel;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.db.models.entity.Question;
import com.nhance.android.enums.BoardType;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven.AttemptStatus;
import com.nhance.android.processors.anaytics.overall.TestOverallTopicAnalyticsProcessor;
import com.nhance.android.utils.SQLDBUtil;

public class TestTopicAnalyticsProcessor extends AbstractAnalyticsProcessor {

    IAnalyticsProcessor overallTopicProcessor;

    public TestTopicAnalyticsProcessor(Context context, Content test, String userId) {

        super(context, test, userId);
        overallTopicProcessor = new TestOverallTopicAnalyticsProcessor(context, userId);
    }

    @Override
    public void process(TakeTestQuestionWithAnswerGiven questionAnsInfo,
            AttemptStatus attemptStatus, Question question) {

        Log.d("TestTopicAnalyticsProcessor", "updating test topic analytics");
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
                    test.orgKeyId, userId, test.id, test.type, b.id, b.type, courseId);
            if (testTopicAnalytics == null) {
                Log.e("TestTopicAnalyticsProcessor", "no test topic analytics found for testId:"
                        + test.id + ", brdId:" + b.id + ", brdType:" + b.type + ", parentId: "
                        + courseId);
                continue;
            }
            updateAbstractAnalyticsModel(questionAnsInfo, testTopicAnalytics, attemptStatus);
            try {
                manager.updateTestBoardAnalytics(testTopicAnalytics);
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        overallTopicProcessor.process(questionAnsInfo, attemptStatus, question);
    }

}
