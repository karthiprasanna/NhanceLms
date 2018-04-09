package com.nhance.android.processors;

import android.util.Log;
import android.widget.ProgressBar;

import com.nhance.android.async.tasks.AbstractVedantuJSONAsyncTask;
import com.nhance.android.db.datamanagers.QuestionAttemptStatusDataManager;
import com.nhance.android.db.models.entity.QuestionStatus;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.VedantuWebUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by prathibha on 9/8/2017.
 */

public class LocalToServerQuestionSyncProcessor extends AbstractVedantuJSONAsyncTask {


    private JSONObject result;
    private JSONArray userAnswer;
    List<QuestionStatus> questionStatusList;
    private QuestionAttemptStatusDataManager questionAttemptStatus;
    private JSONArray answerForCurrentQuestion;


    public LocalToServerQuestionSyncProcessor(SessionManager session, ProgressBar progressUpdater) {
        super(session, progressUpdater);
        questionStatusList = new ArrayList<QuestionStatus>();
        questionAttemptStatus = new QuestionAttemptStatusDataManager(session.getContext());
    }



    @Override
    protected JSONObject doInBackground(String... params) {
        JSONObject jsonRes = null;
        this.url = session.getApiUrl("recordAttempt");
        Map<String, Object> httpParams = new HashMap<String, Object>();

        questionStatusList = questionAttemptStatus.getUnSyncQuestions(50);

        if(questionStatusList != null  && questionStatusList.size()>0) {

            for (int i = 0; i < questionStatusList.size(); i++) {
                QuestionStatus questionStatus = questionStatusList.get(i);
                answerForCurrentQuestion = questionStatus.answerGiven;

                for (int j = 0; j < answerForCurrentQuestion.length(); j++) {
                    try {
                        System.out.println(answerForCurrentQuestion.get(j));
                        httpParams.put("answerGiven[" + j + "]", answerForCurrentQuestion.get(j));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                httpParams.put("qId", questionStatus.id);
                httpParams.put("entityId", questionStatus.id);
                httpParams.put("entityType", "QUESTION");
                httpParams.put("type", questionStatus.questionType);
                httpParams.put("status", "COMPLETE");
                httpParams.put("callingAppId", "TapApp");
                httpParams.put("timeCreated", questionStatus.timeCreated);
                httpParams.put("timeTaken", questionStatus.timeTaken);


                session.addSessionParams(httpParams);
                try {
                    jsonRes = VedantuWebUtils.getJSONData(url, VedantuWebUtils.POST_REQ, httpParams);
                    Log.e("recordAttempt", "..........." + jsonRes);
                    if (jsonRes != null)
                        result = jsonRes.getJSONObject("result");


                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }


        return result;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
        if (jsonObject!=null){

            String questionId = null;
            try {
                questionId = jsonObject.getString("id");
                QuestionStatus questionStatus1 = questionAttemptStatus.getQuestionStatus(questionId);
                Log.e("questionStatus1",".."+questionStatus1);
                questionStatus1.syncStatus = 1;
                questionAttemptStatus.updateQuestionStatus(questionId, questionStatus1);

            } catch (JSONException e) {
                e.printStackTrace();
            }




        }




    }
}
