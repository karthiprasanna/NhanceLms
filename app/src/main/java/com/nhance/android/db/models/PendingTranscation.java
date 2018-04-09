package com.nhance.android.db.models;

import org.json.JSONObject;

import com.nhance.android.enums.TransactionStatus;
import com.nhance.android.pojos.ProgCenterSecInfo;

public class PendingTranscation extends AbstractDataModel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public String             transactionId;
    public String             orderId;
    public String             transactionStatus;
    public JSONObject         progCenterSecInfo;
    public String             transactionInfo;
    public int                step;

    public PendingTranscation() {

        super();
    }

    public PendingTranscation(int orgKeyId, String transactionId, String orderId,
            ProgCenterSecInfo progCenterSecInfo, TransactionStatus transactionStatus) {

        super(String.valueOf(System.currentTimeMillis()), orgKeyId);
        this.transactionId = transactionId;
        this.orderId = orderId;
        this.progCenterSecInfo = progCenterSecInfo.toJSON();
        this.transactionStatus = transactionStatus.name();
    }

    public ProgCenterSecInfo _getProgCenterSecInfo() {

        ProgCenterSecInfo progCenterSecInfo = new ProgCenterSecInfo();
        progCenterSecInfo.fromJSON(this.progCenterSecInfo);
        return progCenterSecInfo;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{transactionId:").append(transactionId).append(", orderId:")
                .append(orderId).append(", transactionStatus:").append(transactionStatus)
                .append(", progCenterSecInfo:").append(progCenterSecInfo)
                .append(", transactionInfo:").append(transactionInfo).append(", step:")
                .append(step).append(", _id:").append(_id).append(", orgKeyId:").append(orgKeyId)
                .append(", timeCreated:").append(timeCreated).append("}");
        return builder.toString();
    }

}
