package com.nhance.android.pojos.onlinedata.tests;

import java.util.List;

import org.json.JSONObject;

import com.nhance.android.utils.JSONAware;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.VedantuWebUtils;

public class EntityMarkDistribution implements JSONAware {

    /**
     * 
     */
    private static final long     serialVersionUID = 1L;

    public float                  avgScore;
    public long                   totalHits;
    public List<MarkDistribution> list;

    @SuppressWarnings("unchecked")
    @Override
    public void fromJSON(JSONObject json) {

        avgScore = (float) JSONUtils.getDouble(json, "avgScore");
        totalHits = JSONUtils.getLong(json, VedantuWebUtils.KEY_TOTAL_HITS);
        list = (List<MarkDistribution>) JSONUtils.getJSONAwareCollection(MarkDistribution.class,
                json, VedantuWebUtils.KEY_LIST);
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }
}
