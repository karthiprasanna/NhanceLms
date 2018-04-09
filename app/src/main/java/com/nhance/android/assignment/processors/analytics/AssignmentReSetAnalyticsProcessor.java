package com.nhance.android.assignment.processors.analytics;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.nhance.android.assignment.db.models.analytics.AssignmentBoardAnalytics;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven;
import com.nhance.android.assignment.pojo.TakeAssignmentQuestionWithAnswerGiven.AssignementAttemptStatus;
import com.nhance.android.assignment.pojo.content.infos.AssignmentExtendedInfo;
import com.nhance.android.assignment.processors.analytics.sync.AssignmentQuestionAnalyticsProcessor;
import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.models.entity.Content;

import org.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Created by Himank Shah on 12/6/2016.
 */

public class AssignmentReSetAnalyticsProcessor implements ITaskProcessor<JSONObject> {

    private final String TAG = "AssignmentReSetAnalyticsProcessor";
    private Content content;
    private Context context;

    public AssignmentReSetAnalyticsProcessor(Context context, Content content) {

        super();
        this.content = content;
        this.context = context;
    }

    @Override
    public void onTaskStart(JSONObject result) {

        if (result == null) {
            return;
        }
        AnalyticsDataManager analyticsManager = new AnalyticsDataManager(context);
        AssignmentExtendedInfo    assignmentInfo = (AssignmentExtendedInfo) content.toContentExtendedInfo();

        Map<AssignmentBoardAnalytics, List<TakeAssignmentQuestionWithAnswerGiven>> boardWiseQuestions = analyticsManager
                .getAssignmentCourseWiseQuestionAnswerGivenMap(assignmentInfo.metadata, content.orgKeyId,
                        content.userId, content.id, content.type);
        ContentDataManager contentManager = new ContentDataManager(context);
        if (boardWiseQuestions != null) {
            AssignmentQuestionAnalyticsProcessor qAnalyticsProcessor = new AssignmentQuestionAnalyticsProcessor(
                    context, content, assignmentInfo.metadata, content.userId);
            Log.i(TAG, "re-setting previously computed local analytics ");
            for (Map.Entry<AssignmentBoardAnalytics, List<TakeAssignmentQuestionWithAnswerGiven>> entry : boardWiseQuestions
                    .entrySet()) {
                for (TakeAssignmentQuestionWithAnswerGiven questionAnsInfo : entry.getValue()) {

                    if (TextUtils.isEmpty(questionAnsInfo.answerGiven)) {
                        continue;
                    }

                    questionAnsInfo.setStatus(AssignementAttemptStatus.RESET);
                    questionAnsInfo.setTimeTaken(-questionAnsInfo.getTimeTaken());
                    qAnalyticsProcessor.process(questionAnsInfo, AssignementAttemptStatus.RESET,
                            questionAnsInfo.getQuestion(content.userId, contentManager));
                }
            }
            boardWiseQuestions.clear();
            boardWiseQuestions = null;
            qAnalyticsProcessor = null;
        }

        assignmentInfo = null;
        content = null;
        contentManager = null;
        analyticsManager = null;
    }

    @Override
    public void onTaskPostExecute(boolean success, JSONObject result) {

    }

}
