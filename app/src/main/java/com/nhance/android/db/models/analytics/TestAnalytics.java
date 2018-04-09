package com.nhance.android.db.models.analytics;

import com.nhance.android.db.models.AbstractTestAnalytics;
import com.nhance.android.utils.SQLDBUtil;

public class TestAnalytics extends AbstractTestAnalytics {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public int                duration;             // this info can be taken from join of test
                                                     // models
    public String             endTime;
    public boolean            synced;

    public TestAnalytics() {

        super(SQLDBUtil.NO_INT_VALUE, null, SQLDBUtil.NO_INT_VALUE, SQLDBUtil.NO_INT_VALUE, null,
                null, SQLDBUtil.NO_INT_VALUE, SQLDBUtil.NO_INT_VALUE, SQLDBUtil.NO_INT_VALUE,
                SQLDBUtil.NO_INT_VALUE);
    }

    public TestAnalytics(int orgKeyId, String userId, int score, float timeTaken, String entityId,
            String entityType, int totalMarks, int qusCount, int attempted, int correct,
            int duration) {

        super(orgKeyId, userId, score, timeTaken, entityId, entityType, totalMarks, qusCount,
                attempted, correct);
        this.duration = duration;
        this.endTime = String.valueOf(0);
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{duration:").append(duration).append(", endTime:").append(endTime)
                .append(", synced:").append(synced).append(", entityId:").append(entityId)
                .append(", entityType:").append(entityType).append(", totalMarks:")
                .append(totalMarks).append(", qusCount:").append(qusCount).append(", attempted:")
                .append(attempted).append(", correct:").append(correct).append(", userId:")
                .append(userId).append(", score:").append(score).append(", timeTaken:")
                .append(timeTaken).append(", _id:").append(_id).append(", orgKeyId:")
                .append(orgKeyId).append(", timeCreated:").append(timeCreated).append("}");
        return builder.toString();
    }

}
