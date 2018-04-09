package com.nhance.android.pojos.tests;

import java.io.Serializable;

import org.json.JSONObject;

import com.nhance.android.utils.JSONAware;
import com.nhance.android.utils.JSONUtils;

public class Marks implements JSONAware, Serializable {

    /**
	 * 
	 */
    private static final long             serialVersionUID = 1L;
    public int                            positive;
    public int                            negative;
    public QuestionResultStatus           status;

    private static transient final String POSITIVE         = "positive";
    private static transient final String NEGATIVE         = "negative";

    public Marks() {

        super();
    }

    public Marks(int positive, int negative) {

        this.positive = positive;
        this.negative = negative;
        this.status = QuestionResultStatus.ACTIVE;
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

    @Override
    public void fromJSON(JSONObject json) {

        positive = JSONUtils.getInt(json, POSITIVE);
        negative = JSONUtils.getInt(json, NEGATIVE);
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{positive:").append(positive).append(", negative:").append(negative)
                .append("}");
        return builder.toString();
    }

}
