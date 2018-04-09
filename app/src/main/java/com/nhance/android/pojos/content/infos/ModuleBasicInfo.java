package com.nhance.android.pojos.content.infos;

import org.json.JSONObject;

import com.nhance.android.enums.ModuleRun;
import com.nhance.android.utils.JSONUtils;

public class ModuleBasicInfo implements IContentInfo {

    public static final String FIELD_TOTAL_CONTENT_COUNT   = "totalContentCount";
    public static final String FIELD_CONSUME_CONTENT_COUNT = "consumedContentCount";
    public static final String FIELD_MODULE_RUN            = "moduleRun";

    /**
     * 
     */
    private static final long  serialVersionUID            = 1L;

    public int                 totalContentCount;
    public int                 consumedContentCount;
    public ModuleRun           moduleRun;

    @Override
    public void fromJSON(JSONObject json) {

        totalContentCount = JSONUtils.getInt(json, FIELD_TOTAL_CONTENT_COUNT);
        consumedContentCount = JSONUtils.getInt(json, FIELD_CONSUME_CONTENT_COUNT);
        moduleRun = ModuleRun.valueOfKey(JSONUtils.getString(json, FIELD_MODULE_RUN));
    }       

    @Override
    public JSONObject toJSON() {

        return null;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{totalContents:").append(totalContentCount)
                .append(", consumedContentCount:").append(consumedContentCount)
                .append(", moduleRun:").append(moduleRun).append("}");
        return builder.toString();
    }

}
