package com.nhance.android.assignment.processors.analytics.overall;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.nhance.android.assignment.db.models.analytics.AssignmentBoardAnalytics;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus;
import com.nhance.android.db.datamanagers.BoardDataManager;
import com.nhance.android.db.models.entity.BoardModel;
import com.nhance.android.db.models.entity.Question;
import com.nhance.android.enums.BoardType;
import com.nhance.android.enums.EntityType;
import com.nhance.android.utils.SQLDBUtil;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Himank Shah on 12/6/2016.
 */

public class AssignmentOverallTopicAnalyticsProcessor extends AssignmentAbstractOverallAnalyticsProcessor {

    public AssignmentOverallTopicAnalyticsProcessor(Context context, String userId) {

        super(context, userId);
    }

    @Override
    public void process(TakeAssignmentQuestionWithAnswerGiven questionAnsInfo,
                        AssignementAttemptStatus attemptStatus, Question question) {

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
            AssignmentBoardAnalytics assignmentTopicAnalytics = manager.getAssignmentSingleBoardAnalytics(
                    question.orgKeyId, userId, entityId, EntityType.ASSIGNMENT.name(), b.id, b.type,
                    courseId);
            if (assignmentTopicAnalytics == null) {
                if (attemptStatus != AssignementAttemptStatus.SAVED) {
                    return;
                }
                assignmentTopicAnalytics = new AssignmentBoardAnalytics(questionAnsInfo.orgKeyId, userId,
                        questionAnsInfo.getScore(), questionAnsInfo.getTimeTaken(), entityId,
                        EntityType.ASSIGNMENT.name(), questionAnsInfo.getMaxMarks(), 1, 1,
                        questionAnsInfo.correct ? 1 : 0, b.id, b.type, b.name, courseId);
                manager.createAssignmentBoardAnalytics(assignmentTopicAnalytics);
                return;
            }
            Log.d("AssignmentOverallTopicAnalyticsProcessor", "updating assignment overalltopic analytics");
            updateAbstractAnalyticsModel(questionAnsInfo, assignmentTopicAnalytics, attemptStatus);
            manager.updateAssignmentBoardAnalytics(assignmentTopicAnalytics);
        }
    }

}
