package com.nhance.android.db.models.entity;

import com.nhance.android.QuestionCount.QuestionCountActivity;
import com.nhance.android.db.models.AbstractDataModel;

import org.json.JSONArray;
import org.json.JSONObject;

public class QuestionStatus extends AbstractDataModel {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public String id;

    public JSONArray correctAnswer;

    public boolean attempted;

    public JSONArray answerGiven;

    public int attempts;

    public String questionType;

    public long timeTaken;

    public int syncStatus;

    public QuestionStatus() {
    }

    public QuestionStatus(String id, JSONArray correctAnswer, boolean attempted, JSONArray answerGiven, int attempts, String timeCreated, long timeTaken, String questionType, int syncStatus) {
        this.id = id;
        this.correctAnswer = correctAnswer;
        this.attempted = attempted;
        this.answerGiven = answerGiven;
        this.attempts = attempts;
        this.timeCreated = timeCreated;
        this.timeTaken = timeTaken;
        this.questionType = questionType;
        this.syncStatus = syncStatus;
    }

    @Override
    public String toString() {
        return "QuestionStatus{" +
                "id='" + id + '\'' +
                ", correctAnswer=" + correctAnswer +
                ", attempted=" + attempted +
                ", answerGiven=" + answerGiven +
                ", attempts=" + attempts +
                ", questionType='" + questionType + '\'' +
                ", timeTaken='" + timeTaken + '\'' +
                ", syncStatus='" + syncStatus + '\'' +
                '}';
    }
}
