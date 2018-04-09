package com.nhance.android.assignment.processors.analytics.overall;

import android.content.Context;
import android.util.Log;

import com.nhance.android.assignment.db.models.analytics.AssignmentBoardAnalytics;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus;
import com.nhance.android.db.models.entity.BoardModel;
import com.nhance.android.db.models.entity.Question;
import com.nhance.android.enums.EntityType;

/**
 * Created by Himank Shah on 12/6/2016.
 */

public class AssignmentOverallCourseAnalyticsProcessor extends AssignmentAbstractOverallAnalyticsProcessor {

    public BoardModel courseModel;
    int               orgKeyId;

    public AssignmentOverallCourseAnalyticsProcessor(Context context, String userId, int orgKeyId) {

        super(context, userId);
        this.orgKeyId = orgKeyId;

    }

    @Override
    public void process(TakeAssignmentQuestionWithAnswerGiven questionAnsInfo,
                        AssignementAttemptStatus attemptStatus, Question question) {

        if (courseModel == null) {
            return;
        }
        AssignmentBoardAnalytics assignmentCourseAnalytics = manager.getAssignmentSingleBoardAnalytics(orgKeyId,
                userId, entityId, EntityType.ASSIGNMENT.name(), courseModel.id, courseModel.type, null);
        if (assignmentCourseAnalytics == null) {
            if (attemptStatus != AssignementAttemptStatus.SAVED) {
                return;
            }
            assignmentCourseAnalytics = new AssignmentBoardAnalytics(orgKeyId, userId,
                    questionAnsInfo.getScore(), questionAnsInfo.getTimeTaken(), entityId,
                    EntityType.ASSIGNMENT.name(), questionAnsInfo.getMaxMarks(), 1, 1,
                    questionAnsInfo.correct ? 1 : 0, courseModel.id, courseModel.type,
                    courseModel.name, null);
            manager.createAssignmentBoardAnalytics(assignmentCourseAnalytics);
            return;
        }
        Log.d("AssignmentOverallCourseAnalyticsProcessor", "updating assignment overallcourse analytics");
        updateAbstractAnalyticsModel(questionAnsInfo, assignmentCourseAnalytics, attemptStatus);
        manager.updateAssignmentBoardAnalytics(assignmentCourseAnalytics);
    }

}
