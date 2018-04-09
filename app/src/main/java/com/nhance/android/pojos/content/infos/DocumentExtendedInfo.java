package com.nhance.android.pojos.content.infos;

import org.json.JSONObject;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.utils.JSONUtils;

public class DocumentExtendedInfo implements IContentInfo {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public int    pages;
    public String url;
    public String uuid;

    @Override
    public void fromJSON(JSONObject json) {

        //pages = JSONUtils.getInt(json, "pages");
        url = JSONUtils.getString(json, ConstantGlobal.URL);
        uuid = JSONUtils.getString(json, ConstantGlobal.UUID);
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

}
