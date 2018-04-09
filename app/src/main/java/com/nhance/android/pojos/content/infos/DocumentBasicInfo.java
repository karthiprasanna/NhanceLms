package com.nhance.android.pojos.content.infos;

import org.json.JSONObject;

import com.nhance.android.utils.JSONUtils;

public class DocumentBasicInfo implements IContentInfo {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public int pages;

    @Override
    public void fromJSON(JSONObject json) {

        pages = JSONUtils.getInt(json, "pages");
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

}
