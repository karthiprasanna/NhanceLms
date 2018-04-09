package com.nhance.android.pojos.content.infos;

import org.json.JSONObject;

import android.text.TextUtils;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.utils.JSONUtils;

public class VideoExtendedInfo implements IContentInfo {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public long   duration;
    public String url;
    public String uuid;
    public String contentType;

    @Override
    public void fromJSON(JSONObject json) {

        duration = JSONUtils.getLong(json, ConstantGlobal.DURATION);
        url = JSONUtils.getString(JSONUtils.getJSONObject(json, "linkInfo"), "embedURL");
        if (TextUtils.isEmpty(url)) {
            url = JSONUtils.getString(json, ConstantGlobal.URL);
        }
        uuid = JSONUtils.getString(json, ConstantGlobal.UUID);
        contentType = JSONUtils.getString(json, ConstantGlobal.CONTENT_TYPE);
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

}
