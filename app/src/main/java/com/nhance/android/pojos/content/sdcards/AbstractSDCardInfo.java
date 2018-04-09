package com.nhance.android.pojos.content.sdcards;

import org.json.JSONObject;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.utils.JSONAware;
import com.nhance.android.utils.JSONUtils;

public abstract class AbstractSDCardInfo implements JSONAware {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public String             name;
    public String             id;
    public long               size;

    @Override
    public void fromJSON(JSONObject json) {

        name = JSONUtils.getString(json, ConstantGlobal.NAME);
        id = JSONUtils.getString(json, ConstantGlobal.ID);
        size = JSONUtils.getLong(json, "size");
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

}
