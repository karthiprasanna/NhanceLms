package com.nhance.android.pojos.onlinedata.tests;

import org.json.JSONObject;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.pojos.content.infos.TestExtendedInfo;
import com.nhance.android.utils.JSONUtils;

public class TestAnalyticsRes extends TestExtendedInfo {

    /**
     * 
     */
    private static final long    serialVersionUID = 1L;

    public OverallEntityMeasures measures;
    public String                id;
    public long                  totalAttempts;

    @Override
    public void fromJSON(JSONObject json) {

        json = JSONUtils.getJSONObject(json, "entity");
        super.fromJSON(json);
        id = JSONUtils.getString(json, ConstantGlobal.ID);
        totalAttempts = JSONUtils.getLong(json, "totalAttempts");
        measures = new OverallEntityMeasures();
        measures.fromJSON(JSONUtils.getJSONObject(json, "measures"));
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

}
