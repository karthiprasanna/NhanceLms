package com.nhance.android.pojos.content.infos;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.nhance.android.pojos.slpmodules.ModuleEntryInfo;
import com.nhance.android.utils.JSONUtils;

public class ModuleExtendedInfo extends ModuleBasicInfo {

    public static final String   FIELD_CHILDREN   = "children";
    /**
     * 
     */
    private static final long    serialVersionUID = 1L;

    public List<ModuleEntryInfo> children;

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        JSONArray childrenJSONArray = JSONUtils.getJSONArray(json, FIELD_CHILDREN);
        if (children == null) {
            children = new ArrayList<ModuleEntryInfo>();
        }
        for (int i = 0; i < childrenJSONArray.length(); i++) {
            ModuleEntryInfo mEntryInfo = new ModuleEntryInfo();
            try {
                mEntryInfo.fromJSON(childrenJSONArray.getJSONObject(i));
                children.add(mEntryInfo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public JSONObject toJSON() {

        return super.toJSON();
    }

}
