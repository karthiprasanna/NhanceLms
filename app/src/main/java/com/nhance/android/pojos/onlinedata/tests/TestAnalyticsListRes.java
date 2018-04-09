package com.nhance.android.pojos.onlinedata.tests;

import java.util.List;

import org.json.JSONObject;

import com.nhance.android.utils.JSONAware;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.VedantuWebUtils;

public class TestAnalyticsListRes implements JSONAware {

    /**
     * 
     */
    private static final long     serialVersionUID = 1L;

    public List<TestAnalyticsRes> list;
    public long                   totalHits;

    @SuppressWarnings("unchecked")
    @Override
    public void fromJSON(JSONObject json) {

        totalHits = JSONUtils.getLong(json, VedantuWebUtils.KEY_TOTAL_HITS);
        list = (List<TestAnalyticsRes>) JSONUtils.getJSONAwareCollection(TestAnalyticsRes.class,
                json, VedantuWebUtils.KEY_LIST);
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

}
