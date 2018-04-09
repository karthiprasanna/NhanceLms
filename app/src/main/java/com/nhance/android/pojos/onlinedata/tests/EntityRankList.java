package com.nhance.android.pojos.onlinedata.tests;

import java.util.List;

import org.json.JSONObject;

import com.nhance.android.pojos.tests.UserEntityRank;
import com.nhance.android.utils.JSONAware;
import com.nhance.android.utils.JSONUtils;
import com.nhance.android.utils.VedantuWebUtils;

public class EntityRankList implements JSONAware {

    /**
     * 
     */
    private static final long   serialVersionUID = 1L;
    public List<UserEntityRank> list;

    @SuppressWarnings("unchecked")
    @Override
    public void fromJSON(JSONObject json) {

        list = (List<UserEntityRank>) JSONUtils.getJSONAwareCollection(UserEntityRank.class, json,
                VedantuWebUtils.KEY_LIST);
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

}
