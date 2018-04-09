package com.nhance.android.db.models;

import com.nhance.android.db.models.analytics.AbstractAnalyticsModel;

public abstract class AbstractTestAnalytics extends AbstractAnalyticsModel {

    /**
     * 
     */
    private static final long serialVersionUID = 7851776643750036941L;

    public String             entityId;
    public String             entityType;

    public int                totalMarks;
    public int                qusCount;
    public int                attempted;
    public int                correct;

    public AbstractTestAnalytics(int orgKeyId, String userId, int score, float timeTaken,
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
