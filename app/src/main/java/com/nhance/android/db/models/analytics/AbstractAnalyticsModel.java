package com.nhance.android.db.models.analytics;

import com.nhance.android.db.models.AbstractDataModel;
import com.nhance.android.interfaces.IAnalyticsInfoPojo;

public abstract class AbstractAnalyticsModel extends AbstractDataModel implements
        IAnalyticsInfoPojo {

    /**
     * 
     */
    private static final long serialVersionUID = -5557192261164292203L;
    public String             userId;
    public int                score;
    public float                timeTaken;                               // in seconds

    public AbstractAnalyticsModel(int orgKeyId, String userId, int score, float timeTaken) {

        this.orgKeyId = orgKeyId;
        this.userId = userId;
        this.score = score;
        this.timeTaken = timeTaken;
    }

}
