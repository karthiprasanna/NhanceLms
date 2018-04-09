package com.nhance.android.pojos.content.infos;

import org.json.JSONObject;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.utils.JSONUtils;

public class VideoBasicInfo implements IContentInfo {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public int duration;

    @Override
    public void fromJSON(JSONObject json) {

        duration = JSONUtils.getInt(json, ConstantGlobal.DURATION);

    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

}
