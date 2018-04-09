package com.nhance.android.pojos.onlinedata.tests;

import org.json.JSONObject;

import com.nhance.android.pojos.tests.EntityMeasures;
import com.nhance.android.utils.JSONUtils;

public class OverallEntityMeasures extends EntityMeasures {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public int                maxScore;

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        maxScore = JSONUtils.getInt(json, "maxScore");
    }

}
