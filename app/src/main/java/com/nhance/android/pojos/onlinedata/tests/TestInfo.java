package com.nhance.android.pojos.onlinedata.tests;

import org.json.JSONObject;

import com.nhance.android.pojos.UserInfoRes;
import com.nhance.android.pojos.content.infos.TestExtendedInfo;
import com.nhance.android.utils.JSONUtils;

public class TestInfo extends TestExtendedInfo {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public float              avgMarks;
    public long               avgTimeTaken;
    public long               attempts;
    public UserInfoRes        user;

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        avgMarks = (float) JSONUtils.getDouble(json, "avgMarks");
        avgTimeTaken = JSONUtils.getLong(json, "avgTimeTaken");
        attempts = JSONUtils.getLong(json, "attempts");
        JSONObject userJSON = JSONUtils.getJSONObject(json, "user");
        if (userJSON != null && userJSON.length() > 0) {
            user = new UserInfoRes();
            user.fromJSON(userJSON);
        }
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

}
