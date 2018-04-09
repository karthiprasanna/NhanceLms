package com.nhance.android.assignment.db.models.analytics;

import com.nhance.android.utils.SQLDBUtil;

/**
 * Created by Himank Shah on 11/30/2016.
 */

public class AssignmentAnalytics extends AbstractAssignmentAnalytics {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public int duration;             // this info can be taken from join of test
    // models
    public String endTime;
    public boolean synced;

    public AssignmentAnalytics() {

        super(SQLDBUtil.NO_INT_VALUE, null, SQLDBUtil.NO_INT_VALUE, SQLDBUtil.NO_INT_VALUE, null,
                null, SQLDBUtil.NO_INT_VALUE, SQLDBUtil.NO_INT_VALUE, SQLDBUtil.NO_INT_VALUE,
                SQLDBUtil.NO_INT_VALUE);
    }

    public AssignmentAnalytics(int orgKeyId, String userId, int score, int timeTaken, String entityId,
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
