package com.nhance.android.db.models.entity;

import org.json.JSONObject;

import com.nhance.android.db.models.AbstractDataModel;

public class Answer extends AbstractDataModel {

    /**
     * 
     */
    private static final long serialVersionUID = 4567310476499102070L;

    public String             ansId;
    public String             answer;
    public JSONObject         matrixAnswer;
    public JSONObject         solution;
    public String             userId;
    public String             qId;

    public Answer() {

        super();
    }

    public Answer(String timeCreated, int orgKeyId, String ansId, String answer,
            JSONObject matrixAnswer, JSONObject solution, String userId, String qId) {

        super(timeCreated, orgKeyId);
        this.ansId = ansId;
        this.answer = answer;
        this.matrixAnswer = matrixAnswer;
        this.solution = solution;
        this.userId = userId;
        this.qId = qId;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{ansId:").append(ansId).append(", answer:").append(answer)
                .append(", matrixAnswer:").append(matrixAnswer).append(", solution:")
                .append(solution).append(", userId:").append(userId).append(", qId:").append(qId)
                .append(", _id:").append(_id).append(", orgKeyId:").append(orgKeyId)
                .append(", timeCreated:").append(timeCreated).append("}");
        return builder.toString();
    }

}
