package com.nhance.android.processors.anaytics.overall;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.nhance.android.db.datamanagers.BoardDataManager;
import com.nhance.android.db.models.analytics.TestCourseLevelAnalytics;
import com.nhance.android.db.models.entity.BoardModel;
import com.nhance.android.db.models.entity.Question;
import com.nhance.android.enums.BoardType;
import com.nhance.android.enums.EntityType;
import com.nhance.android.enums.QuestionLevel;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven.AttemptStatus;
import com.nhance.android.utils.SQLDBUtil;

public class TestOverallCourseWiseLevelAnalyticsProcessor extends AbstractOverallAnalyticsProcessor {

    public TestOverallCourseWiseLevelAnalyticsProcessor(Context context, String userId) {

        super(context, userId);
    }

    @Override
    public void process(TakeTestQuestionWithAnswerGiven questionAnsInfo,
            AttemptStatus attemptStatus, Question question) {

        QuestionLevel level = QuestionLevel.valueOfKey(question.difficulty);
        BoardModel board = getCourseBoard(question.brdIds);
        if (board == null) {
            Log.e("TestOverallCourseWiseLevelAnalyticsProcessor", "no course found for brdIds: "
                    + question.brdIds);
            return;
        }

        TestCourseLevelAnalytics courseLevelAnalytics = manager.getTestCourseLevelAnalytics(
                question.orgKeyId, userId, entityId, EntityType.TEST.name(), board.id, board.type,
                level);
        if (courseLevelAnalytics == null) {
            courseLevelAnalytics = new TestCourseLevelAnalytics(question.orgKeyId, userId,
                    questionAnsInfo.getScore(), questionAnsInfo.getTimeTaken(), entityId,
                    EntityType.TEST.name(), questionAnsInfo.getMaxMarks(), 1, 1,
                    questionAnsInfo.correct ? 1 : 0, level, board.id, board.type, board.name);
            try {
                manager.createTestCourseLevelAnalytics(courseLevelAnalytics);
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            updateAbstractAnalyticsModel(questionAnsInfo, courseLevelAnalytics, attemptStatus);
            try {
                manager.updateCourseLevelAnalytics(courseLevelAnalytics);
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
