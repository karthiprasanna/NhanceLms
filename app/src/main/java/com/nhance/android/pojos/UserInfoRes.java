package com.nhance.android.pojos;

import org.json.JSONObject;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.utils.JSONAware;
import com.nhance.android.utils.JSONUtils;

public class UserInfoRes implements JSONAware {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public String firstName;
    public String lastName;
    public String thumbnail;

    @Override
    public void fromJSON(JSONObject json) {

        firstName = JSONUtils.getString(json, ConstantGlobal.FIRST_NAME);
        lastName = JSONUtils.getString(json, ConstantGlobal.LAST_NAME);
        thumbnail = JSONUtils.getString(json, ConstantGlobal.THUMBNAIL);
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }
}
