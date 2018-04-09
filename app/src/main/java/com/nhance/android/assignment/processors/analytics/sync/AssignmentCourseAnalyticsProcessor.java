package com.nhance.android.assignment.processors.analytics.sync;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.nhance.android.assignment.db.models.analytics.AssignmentBoardAnalytics;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus;
import com.nhance.android.assignment.pojo.assignment.AssignmentMetadata;
import com.nhance.android.assignment.processors.analytics.overall.AssignmentOverallCourseAnalyticsProcessor;
import com.nhance.android.db.datamanagers.BoardDataManager;
import com.nhance.android.db.models.entity.BoardModel;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.db.models.entity.Question;
import com.nhance.android.enums.BoardType;
import com.nhance.android.utils.SQLDBUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Himank Shah on 12/6/2016.
 */

public class AssignmentCourseAnalyticsProcessor extends AssignmentAbstractAnalyticsProcessor {

    private final String TAG = "AssignmentCourseAnalyticsProcessor";
    AssignmentOverallCourseAnalyticsProcessor overallCourseAnalyticProcessor;

    public AssignmentCourseAnalyticsProcessor(Context context, Content assignment,
                                              List<AssignmentMetadata> courseMetadata, String userId) {

        super(context, assignment, userId);
        manager.createAssignmentCourseAnalytics(assignment, courseMetadata, userId);
        overallCourseAnalyticProcessor = new AssignmentOverallCourseAnalyticsProcessor(context, userId,
                assignment.orgKeyId);
    }

    @Override
    public void process(TakeAssignmentQuestionWithAnswerGiven questionAnsInfo,
                        AssignementAttemptStatus attemptStatus, Question question) {

        BoardModel board = getCourseBoard(question.brdIds);
        if (board == null) {
            Log.e(TAG, "no course found for brdIds: " + question.brdIds);
            return;
        }
        overallCourseAnalyticProcessor.courseModel = board;
        AssignmentBoardAnalytics assignmentCourseAnalytics = manager.getAssignmentSingleBoardAnalytics(assignment.orgKeyId,
                userId, assignment.id, assignment.type, board.id, board.type, null);
        if (assignmentCourseAnalytics == null) {
            Log.e(TAG, "no assignment course analytics found for assignmentId:" + assignment.id + ", brdId:"
                    + board.id + ", brdType:" + board.type);
            return;
        }
        Log.d(TAG, "updating assignment course analytics");
        updateAbstractAnalyticsModel(questionAnsInfo, assignmentCourseAnalytics, attemptStatus);
        manager.updateAssignmentBoardAnalytics(assignmentCourseAnalytics);
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
