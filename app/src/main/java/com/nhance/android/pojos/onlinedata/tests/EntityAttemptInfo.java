package com.nhance.android.pojos.onlinedata.tests;

import org.json.JSONObject;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.pojos.tests.EntityMeasures;
import com.nhance.android.utils.JSONAware;
import com.nhance.android.utils.JSONUtils;

public class EntityAttemptInfo implements JSONAware {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public EntityMeasures     measures;
    public String             id;
    public int                qusNo;

    @Override
    public void fromJSON(JSONObject json) {

        measures = new EntityMeasures();
        measures.fromJSON(JSONUtils.getJSONObject(json, "measures"));
        JSONObject info = JSONUtils.getJSONObject(json, ConstantGlobal.INFO);
        id = JSONUtils.getString(info, ConstantGlobal.ID);
        qusNo = JSONUtils.getInt(json, "qusNo");

    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

}
