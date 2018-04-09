package com.nhance.android.pojos.onlinedata.tests;

import java.util.List;

import org.json.JSONObject;

import com.nhance.android.utils.JSONAware;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.VedantuWebUtils;

public class EntityAttemptsInfoRes implements JSONAware {

    /**
     * 
     */
    private static final long      serialVersionUID = 1L;

    public int                     totalHits;
    public List<EntityAttemptInfo> list;

    @SuppressWarnings("unchecked")
    @Override
    public void fromJSON(JSONObject json) {

        totalHits = JSONUtils.getInt(json, VedantuWebUtils.KEY_TOTAL_HITS);
        list = (List<EntityAttemptInfo>) JSONUtils.getJSONAwareCollection(EntityAttemptInfo.class,
                json, VedantuWebUtils.KEY_LIST);
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

}
