package com.nhance.android.db.models.analytics;

import com.nhance.android.db.models.AbstractTestAnalytics;
import com.nhance.android.enums.QuestionLevel;
import com.nhance.android.utils.SQLDBUtil;

public class TestCourseLevelAnalytics extends AbstractTestAnalytics {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public String             level;
    public String             id;                   // course brdId
    public String             type;
    public String             name;

    public TestCourseLevelAnalytics() {

        super(SQLDBUtil.NO_INT_VALUE, null, SQLDBUtil.NO_INT_VALUE, SQLDBUtil.NO_INT_VALUE, null,
                null, SQLDBUtil.NO_INT_VALUE, SQLDBUtil.NO_INT_VALUE, SQLDBUtil.NO_INT_VALUE,
                SQLDBUtil.NO_INT_VALUE);
    }

    public TestCourseLevelAnalytics(int orgKeyId, String userId, int score, float timeTaken,
            String entityId, String entityType, int totalMarks, int qusCount, int attempted,
            int correct, QuestionLevel level, String id, String type, String name) {

        super(orgKeyId, userId, score, timeTaken, entityId, entityType, totalMarks, qusCount,
                attempted, correct);
        this.level = level.name();
        this.id = id;
        this.type = type;
        this.name = name;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{level:").append(level).append(", id:").append(id).append(", type:")
                .append(type).append(", name:").append(name).append(", entityId:").append(entityId)
                .append(", entityType:").append(entityType).append(", totalMarks:")
                .append(totalMarks).append(", qusCount:").append(qusCount).append(", attempted:")
                .append(attempted).append(", correct:").append(correct).append(", userId:")
                .append(userId).append(", score:").append(score).append(", timeTaken:")
                .append(timeTaken).append(", _id:").append(_id).append(", orgKeyId:")
                .append(orgKeyId).append(", timeCreated:").append(timeCreated).append("}");
        return builder.toString();
    }

}
