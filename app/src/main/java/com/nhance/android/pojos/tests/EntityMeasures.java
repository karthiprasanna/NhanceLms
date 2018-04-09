package com.nhance.android.pojos.tests;

import org.json.JSONObject;

import com.nhance.android.utils.JSONAware;
import com.nhance.android.utils.JSONUtils;

public class EntityMeasures implements JSONAware {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public int                score;
    public long               attempts;

    public long               correct;
    public long               incorrect;
    public long               left;

    public long               timeTaken;

    @Override
    public void fromJSON(JSONObject json) {

        score = JSONUtils.getInt(json, "score");
        attempts = JSONUtils.getLong(json, "attempts");
        correct = JSONUtils.getLong(json, "correct");
        incorrect = JSONUtils.getLong(json, "incorrect");
        left = JSONUtils.getLong(json, "left");
        timeTaken = JSONUtils.getLong(json, "timeTaken");
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

}
