package com.nhance.android.billing.payments;

import java.io.Serializable;

import org.json.JSONObject;

import android.util.Log;

import com.nhance.android.utils.JSONUtils;

public class PaymentResult implements Serializable {

    /**
     * 
     */
    private static final long  serialVersionUID               = 1L;
    public static final String TRANSACTION_STATUS_NOT_STARTED = "NOT_STARTED";
    public static final String TRANSACTION_STATUS_SUCCESS     = "SUCCESS";
    public static final String TRANSACTION_STATUS_CANCELLED   = "CANCELLED";
    public static final String TRANSACTION_STATUS_FAILED      = "FAILED";

    public String              item_sku;
    public String              transactionId;
    public String              transactionStatus;
    public String              errorMessage;

    public PaymentResult() {

    }

    public PaymentResult(String jsonObject) {

        try {
            JSONObject json = new JSONObject(jsonObject);
            Log.d("PaymentResult", "jsonObject : " + jsonObject);
            item_sku = JSONUtils.getString(json, "item_sku");
            transactionId = JSONUtils.getString(json, "transactionId");
            transactionStatus = JSONUtils.getString(json, "transactionStatus");
            errorMessage = JSONUtils.getString(json, "errorMessage");
        } catch (Throwable e) {
            Log.e("PaymentResult", e.getMessage(), e);
        }

    }

    public boolean isSuccess() {

        return transactionStatus != null && transactionStatus.equals(TRANSACTION_STATUS_SUCCESS);
    }

    public boolean isFailure() {

        return !isSuccess();
    }

    public boolean isCancelled() {

        return transactionStatus != null && transactionStatus.equals(TRANSACTION_STATUS_CANCELLED);
    }

    @Override
    public String toString() {

        // do not change this toString format as this is directly saved in local db
        StringBuilder builder = new StringBuilder();
        builder.append("{\"item_sku\" : \"").append(item_sku).append("\", \"transactionId\" : \"")
                .append(transactionId).append("\", \"transactionStatus\" : \"")
                .append(transactionStatus).append("\", \"errorMessage\" :\"").append(errorMessage)
                .append("\"}");
        return builder.toString();
    }

    public String getOriginalJson() {

        return toString();
    }

}
