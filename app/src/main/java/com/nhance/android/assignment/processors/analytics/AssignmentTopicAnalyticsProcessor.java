package com.nhance.android.assignment.processors.analytics;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.nhance.android.assignment.db.models.analytics.AssignmentBoardAnalytics;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus;
import com.nhance.android.assignment.processors.analytics.overall.AssignmentOverallTopicAnalyticsProcessor;
import com.nhance.android.assignment.processors.analytics.sync.AssignmentAbstractAnalyticsProcessor;
import com.nhance.android.assignment.processors.analytics.sync.AssignmentIAnalyticsProcessor;
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

public class AssignmentTopicAnalyticsProcessor extends AssignmentAbstractAnalyticsProcessor {

    AssignmentIAnalyticsProcessor overallTopicProcessor;

    public AssignmentTopicAnalyticsProcessor(Context context, Content assignment, String userId) {

        super(context, assignment, userId);
        overallTopicProcessor = new AssignmentOverallTopicAnalyticsProcessor(context, userId);
    }

    @Override
    public void process(TakeAssignmentQuestionWithAnswerGiven questionAnsInfo,
                        AssignementAttemptStatus attemptStatus, Question question) {

        Log.d("AssignmentTopicAnalyticsProcessor", "updating assignment topic analytics");
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
                    assignment.orgKeyId, userId, assignment.id, assignment.type, b.id, b.type, courseId);
            if (assignmentTopicAnalytics == null) {
                Log.e("AssignmentTopicAnalyticsProcessor", "no assignment topic analytics found for assignmentId:"
                        + assignment.id + ", brdId:" + b.id + ", brdType:" + b.type + ", parentId: "
                        + courseId);
                continue;
            }
            updateAbstractAnalyticsModel(questionAnsInfo, assignmentTopicAnalytics, attemptStatus);
            manager.updateAssignmentBoardAnalytics(assignmentTopicAnalytics);
        }
        overallTopicProcessor.process(questionAnsInfo, attemptStatus, question);
    }

}
