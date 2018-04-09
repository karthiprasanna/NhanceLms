package com.nhance.android.pojos;

import java.io.Serializable;

import org.json.JSONObject;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.utils.JSONAware;
import com.nhance.android.utils.JSONUtils;

public class CostRate implements JSONAware, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public int                value;                // amount in paisa
    public String             unit;                 // e.g per user per month
    public String             currencyCode;

    public CostRate() {

        super();
    }

    public CostRate(int value, String unit, String currencyCode) {

        super();
        this.value = value;
        this.unit = unit;
        this.currencyCode = currencyCode;
    }

    @Override
    public void fromJSON(JSONObject json) {

        this.value = JSONUtils.getInt(json, ConstantGlobal.VALUE);
        this.currencyCode = JSONUtils.getString(json, ConstantGlobal.CURRENCY_CODE);
        // TODO: use unit if required
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

}
