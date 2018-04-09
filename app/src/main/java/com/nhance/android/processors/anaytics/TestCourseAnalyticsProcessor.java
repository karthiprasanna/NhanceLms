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
import com.nhance.android.pojos.tests.TestMetadata;
import com.nhance.android.processors.anaytics.overall.TestOverallCourseAnalyticsProcessor;
import com.nhance.android.utils.SQLDBUtil;

public class TestCourseAnalyticsProcessor extends AbstractAnalyticsProcessor {

    private final String                TAG = "TestCourseAnalyticsProcessor";
    TestOverallCourseAnalyticsProcessor overallCourseAnalyticProcessor;

    public TestCourseAnalyticsProcessor(Context context, Content test,
            List<TestMetadata> courseMetadata, String userId) {

        super(context, test, userId);
        manager.createTestCourseAnalytics(test, courseMetadata, userId);
        overallCourseAnalyticProcessor = new TestOverallCourseAnalyticsProcessor(context, userId,
                test.orgKeyId);
    }

    @Override
    public void process(TakeTestQuestionWithAnswerGiven questionAnsInfo,
            AttemptStatus attemptStatus, Question question) {

        BoardModel board = getCourseBoard(question.brdIds);
        if (board == null) {
            Log.e(TAG, "no course found for brdIds:" + question.brdIds);
            return;
        }
        overallCourseAnalyticProcessor.courseModel = board;
        TestBoardAnalytics testCourseAnalytics = manager.getTestSingleBoardAnalytics(test.orgKeyId,
                userId, test.id, test.type, board.id, board.type, null);
        if (testCourseAnalytics == null) {
            Log.e(TAG, "no test course analytics found for testId:" + test.id + ", brdId:"
                    + board.id + ", brdType:" + board.type);
            return;
        }
        Log.d(TAG, "updating test course analytics");
        updateAbstractAnalyticsModel(questionAnsInfo, testCourseAnalytics, attemptStatus);
        try {
            manager.updateTestBoardAnalytics(testCourseAnalytics);
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        overallCourseAnalyticProcessor.process(questionAnsInfo, attemptStatus, question);
    }

    private BoardModel getCourseBoard(String brdIds) {

        List<BoardModel> boards = new BoardDataManager(context).getBoards(Arrays.asList(TextUtils
                .split(brdIds, SQLDBUtil.SEPARATOR)));
        if (boards != null) {
            for (BoardModel b : boards) {
                if (BoardType.COURSE.name().equals(b.type)) {
                    return b;
                }
            }
        }
        return null;
    }
}
