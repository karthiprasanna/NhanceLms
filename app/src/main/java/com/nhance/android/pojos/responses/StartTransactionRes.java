package com.nhance.android.pojos.responses;

import org.json.JSONObject;

import com.nhance.android.billing.payments.PaymentHandler;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.utils.JSONAware;
import com.nhance.android.utils.JSONUtils;

public class StartTransactionRes implements JSONAware {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public String             transactionId;
    public String             orderId;
    public String             paymentUrl;
    public String             email;
    public boolean            needEmail;

    @Override
    public void fromJSON(JSONObject json) {

        transactionId = JSONUtils.getString(json, ConstantGlobal.TRANSACTION_ID);
        orderId = JSONUtils.getString(json, ConstantGlobal.ORDER_ID);
        paymentUrl = JSONUtils.getString(json, PaymentHandler.FIELD_PAYMENT_URL);
        email = JSONUtils.getString(json, "email");
        needEmail = JSONUtils.getBoolean(json, "needEmail");
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

}
