package com.nhance.android.processors.anaytics;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.nhance.android.async.tasks.ITaskProcessor;
import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.models.analytics.TestBoardAnalytics;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven;
import com.nhance.android.pojos.TakeTestQuestionWithAnswerGiven.AttemptStatus;
import com.nhance.android.pojos.content.infos.TestExtendedInfo;

public class ReSetAnalyticsProcessor implements ITaskProcessor<JSONObject> {

    private final String TAG = "ReSetAnalyticsProcessor";
    private Content      content;
    private Context      context;

    public ReSetAnalyticsProcessor(Context context, Content content) {

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
        TestExtendedInfo testInfo = (TestExtendedInfo) content.toContentExtendedInfo();

        Map<TestBoardAnalytics, List<TakeTestQuestionWithAnswerGiven>> boardWiseQuestions = analyticsManager
                .getTestCourseWiseQuestionAnswerGivenMap(testInfo.metadata, content.orgKeyId,
                        content.userId, content.id, content.type);
        ContentDataManager contentManager = new ContentDataManager(context);
        if (boardWiseQuestions != null) {
            QuestionAnalyticsProcessor qAnalyticsProcessor = new QuestionAnalyticsProcessor(
                    context, content, testInfo.metadata, content.userId);
            Log.i(TAG, "re-setting previously computed local analytics ");
            for (Entry<TestBoardAnalytics, List<TakeTestQuestionWithAnswerGiven>> entry : boardWiseQuestions
                    .entrySet()) {
                for (TakeTestQuestionWithAnswerGiven questionAnsInfo : entry.getValue()) {

                    if (TextUtils.isEmpty(questionAnsInfo.answerGiven)) {
                        continue;
                    }

                    questionAnsInfo.setStatus(AttemptStatus.RESET);
                    questionAnsInfo.setTimeTaken(-questionAnsInfo.getTimeTaken());
                    qAnalyticsProcessor.process(questionAnsInfo, AttemptStatus.RESET,
                            questionAnsInfo.getQuestion(content.userId, contentManager));
                }
            }
            boardWiseQuestions.clear();
            boardWiseQuestions = null;
            qAnalyticsProcessor = null;
        }

        testInfo = null;
        content = null;
        contentManager = null;
        analyticsManager = null;
    }

    @Override
    public void onTaskPostExecute(boolean success, JSONObject result) {

    }

}
