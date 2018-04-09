package com.nhance.android.assignment.db.models.analytics;

import com.nhance.android.db.models.analytics.AbstractAnalyticsModel;

/**
 * Created by Himank Shah on 11/30/2016.
 */

public class AbstractAssignmentAnalytics extends AbstractAnalyticsModel {

    /**
     *
     */
    private static final long serialVersionUID = 7851776643750036941L;

    public String entityId;
    public String entityType;

    public int totalMarks;
    public int qusCount;
    public int attempted;
    public int correct;

    public AbstractAssignmentAnalytics(int orgKeyId, String userId, int score, float timeTaken,
                                       String entityId, String entityType, int totalMarks, int qusCount, int attempted,
                                       int correct) {

        super(orgKeyId, userId, score, timeTaken);
        this.entityId = entityId;
        this.entityType = entityType;
        this.totalMarks = totalMarks;
        this.qusCount = qusCount;
        this.attempted = attempted;
        this.correct = correct;
    }

}
