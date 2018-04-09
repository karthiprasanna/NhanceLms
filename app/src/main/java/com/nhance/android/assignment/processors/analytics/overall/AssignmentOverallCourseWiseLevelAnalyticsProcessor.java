package com.nhance.android.assignment.processors.analytics.overall;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.nhance.android.assignment.db.models.analytics.AssignmentCourseLevelAnalytics;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus;
import com.nhance.android.db.datamanagers.BoardDataManager;
import com.nhance.android.db.models.entity.BoardModel;
import com.nhance.android.db.models.entity.Question;
import com.nhance.android.enums.BoardType;
import com.nhance.android.enums.EntityType;
import com.nhance.android.enums.QuestionLevel;
import com.nhance.android.utils.SQLDBUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Himank Shah on 12/6/2016.
 */

public class AssignmentOverallCourseWiseLevelAnalyticsProcessor extends AssignmentAbstractOverallAnalyticsProcessor {

    public AssignmentOverallCourseWiseLevelAnalyticsProcessor(Context context, String userId) {

        super(context, userId);
    }

    @Override
    public void process(TakeAssignmentQuestionWithAnswerGiven questionAnsInfo,
                        AssignementAttemptStatus attemptStatus, Question question) {

        QuestionLevel level = QuestionLevel.valueOfKey(question.difficulty);
        BoardModel board = getCourseBoard(question.brdIds);
        if (board == null) {
            Log.e("AssignmentOverallCourseWiseLevelAnalyticsProcessor", "no course found for brdIds: "
                    + question.brdIds);
            return;
        }

        AssignmentCourseLevelAnalytics courseLevelAnalytics = manager.getAssignmentCourseLevelAnalytics(
                question.orgKeyId, userId, entityId, EntityType.ASSIGNMENT.name(), board.id, board.type,
                level);
        if (courseLevelAnalytics == null) {
            courseLevelAnalytics = new AssignmentCourseLevelAnalytics(question.orgKeyId, userId,
                    questionAnsInfo.getScore(), questionAnsInfo.getTimeTaken(), entityId,
                    EntityType.ASSIGNMENT.name(), questionAnsInfo.getMaxMarks(), 1, 1,
                    questionAnsInfo.correct ? 1 : 0, level, board.id, board.type, board.name);
            manager.createAssignmentCourseLevelAnalytics(courseLevelAnalytics);
        } else {
            updateAbstractAnalyticsModel(questionAnsInfo, courseLevelAnalytics, attemptStatus);
            manager.updateCourseLevelAnalytics(courseLevelAnalytics);
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

