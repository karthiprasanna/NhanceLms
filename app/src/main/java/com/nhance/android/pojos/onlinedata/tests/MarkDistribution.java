package com.nhance.android.pojos.onlinedata.tests;

import org.json.JSONObject;

import com.nhance.android.utils.JSONAware;
import com.nhance.android.utils.JSONUtils;

public class MarkDistribution implements JSONAware {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public int                from;
    public int                to;
    public int                count;

    @Override
    public void fromJSON(JSONObject json) {

        from = JSONUtils.getInt(json, "from");
        to = JSONUtils.getInt(json, "to");
        count = JSONUtils.getInt(json, "count");

    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{from:").append(from).append(", to:").append(to).append(", count:")
                .append(count).append("}");
        return builder.toString();
    }

}
