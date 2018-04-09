package com.nhance.android.db.models.analytics;

public class QuestionAnalytics extends AbstractAnalyticsModel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public String             id;                   // qId
    public boolean            correct;
    public String             entityId;
    public String             entityType;
    public String             answerGiven;
    public int                qusNo;
    public boolean            synced;

    public QuestionAnalytics() {

        super(-1, null, -1, -1);
    }

    public QuestionAnalytics(int orgKeyId, String userId, String entityId, String entityType,
            int score, float timeTaken, String id, boolean correct, String answerGiven, int qusNo) {

        super(orgKeyId, userId, score, timeTaken);
        this.id = id;
        this.correct = correct;
        this.answerGiven = answerGiven;
        this.entityId = entityId;
        this.entityType = entityType;
        this.qusNo = qusNo;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{id:").append(id).append(", correct:").append(correct)
                .append(", entityId:").append(entityId).append(", entityType:").append(entityType)
                .append(", answerGiven:").append(answerGiven).append(", qusNo:").append(qusNo)
                .append(", synced:").append(synced).append(", userId:").append(userId)
                .append(", score:").append(score).append(", timeTaken:").append(timeTaken)
                .append(", _id:").append(_id).append(", orgKeyId:").append(orgKeyId)
                .append(", qusNo:").append(qusNo).append(", timeCreated:").append(timeCreated)
                .append("}");
        return builder.toString();
    }

}
