package com.nhance.android.pojos.content.infos;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.utils.JSONUtils;

import org.json.JSONObject;

/**
 * Created by ragunath on 2/23/2017.
 */

public class HtmlContentExtendedInfo implements IContentInfo {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public String url;
    public String uuid;
    public String extention;

    @Override
    public void fromJSON(JSONObject json) {

        url = JSONUtils.getString(json, ConstantGlobal.URL);
        uuid = JSONUtils.getString(json, ConstantGlobal.UUID);
        extention = JSONUtils.getString(json, "extention");
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

}
