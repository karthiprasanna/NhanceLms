package com.nhance.android.pojos.tests;

import org.json.JSONObject;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.pojos.UserInfoRes;
import com.nhance.android.utils.JSONAware;
import com.nhance.android.utils.JSONUtils;

public class UserEntityRank implements JSONAware {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public int                rank;                 // rank in the entity.type
    public long               startTime;
    public long               endTime;
    public EntityMeasures     measures;
    public UserInfoRes        user;

    @Override
    public void fromJSON(JSONObject json) {

        rank = JSONUtils.getInt(json, "rank");

        JSONObject infoJSON = JSONUtils.getJSONObject(json, "info");
        startTime = JSONUtils.getLong(infoJSON, ConstantGlobal.START_TIME);
        endTime = JSONUtils.getLong(infoJSON, ConstantGlobal.END_TIME);
        measures = new EntityMeasures();
        measures.fromJSON(JSONUtils.getJSONObject(infoJSON, "measures"));
        user = new UserInfoRes();
        user.fromJSON(JSONUtils.getJSONObject(json, "user"));
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

}
