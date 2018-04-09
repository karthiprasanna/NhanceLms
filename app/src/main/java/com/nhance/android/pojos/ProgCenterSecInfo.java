package com.nhance.android.pojos;

import java.io.Serializable;

import org.json.JSONObject;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.enums.SectionAccessScope;
import com.nhance.android.enums.SectionRevenueModel;
import com.nhance.android.utils.JSONAware;
import com.nhance.android.utils.JSONUtils;

public class ProgCenterSecInfo implements Serializable, JSONAware {

    /**
     * 
     */
    private static final long  serialVersionUID = 1L;
    public String              programId;
    public String              centerId;
    public String              sectionId;
    public CostRate            costRate;
    public SectionRevenueModel revenueModel;
    public SectionAccessScope  accessScope;
    public String              displayName;

    public ProgCenterSecInfo() {

        super();
    }

    public ProgCenterSecInfo(String programId, String centerId, String sectionId,
            SectionAccessScope accessScope, SectionRevenueModel revenueModel, CostRate costRate) {

        this.programId = programId;
        this.centerId = centerId;
        this.sectionId = sectionId;
        this.accessScope = accessScope;
        this.revenueModel = revenueModel;
        this.costRate = costRate;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{programId:").append(programId).append(", centerId:").append(centerId)
                .append(", sectionId:").append(sectionId).append(", costRate:").append(costRate)
                .append(", revenueModel:").append(revenueModel).append(", accessScope:")
                .append(accessScope).append("}");
        return builder.toString();
    }

    @Override
    public void fromJSON(JSONObject json) {

        programId = JSONUtils.getString(json, ConstantGlobal.PROGRAM_ID);
        centerId = JSONUtils.getString(json, "centerId");
        sectionId = JSONUtils.getString(json, "sectionId");
        JSONObject cRate = JSONUtils.getJSONObject(json, ConstantGlobal.COST_RATE);
        costRate = new CostRate();
        costRate.fromJSON(cRate);
        revenueModel = SectionRevenueModel.valueOfKey(JSONUtils.getString(json,
                ConstantGlobal.REVENUE_MODEL));
        accessScope = SectionAccessScope.valueOfKey(JSONUtils.getString(json,
                ConstantGlobal.ACCESS_SCOPE));
        displayName = JSONUtils.getString(json, "displayName");

    }

    @Override
    public JSONObject toJSON() {

        JSONObject json = new JSONObject();
        JSONUtils.putValue(ConstantGlobal.PROGRAM_ID, programId, json);
        JSONUtils.putValue("centerId", centerId, json);
        JSONUtils.putValue("sectionId", sectionId, json);
        if (costRate != null) {
            JSONUtils.putValue(ConstantGlobal.COST_RATE, costRate.toJSON(), json);
        }
        JSONUtils.putValue(ConstantGlobal.REVENUE_MODEL, revenueModel.name(), json);
        JSONUtils.putValue(ConstantGlobal.ACCESS_SCOPE, accessScope.name(), json);
        JSONUtils.putValue("displayName", displayName, json);

        return json;
    }
}
