package com.nhance.android.billing.payments;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.nhance.android.activities.PaymentFragmentActivity;
import com.nhance.android.constants.ConstantGlobal;

public class PaymentHandler {

    private static final String TAG               = "PaymentHandler";
    public static final int     REQUEST_CODE      = 10001;
    public static final String  FIELD_PAYMENT_URL = "paymentUrl";

    // Context we were passed during initialization
    boolean                     mSetupDone;
    boolean                     mDebugLog;
    OnPaymentFinishedListner    mOnPaymentFinishedListner;
    int                         mRequestCode;

    public PaymentHandler() {

        super();
    }

    public void enableDebugLogging(boolean enable) {

        mDebugLog = enable;
    }

    public void setUp() {

        mSetupDone = true;
    }

    void checkSetupDone(String operation) {

        if (!mSetupDone) {
            Log.e(TAG, "Illegal state for operation (" + operation
                    + "): Payment handler  is not set up.");
            throw new IllegalStateException(
                    "Payment handler is not set up. Can't perform operation: " + operation);
        }
    }

    public void startPayment(final String billingEmail, final String paymentUrl,
            FragmentActivity activity, String userId, String sku, String transactionId,
            int requestCode, OnPaymentFinishedListner onPaymentFinished) {

        checkSetupDone("startPayment");
        this.mOnPaymentFinishedListner = onPaymentFinished;
        this.mRequestCode = requestCode;

        Intent intent = new Intent(activity, PaymentFragmentActivity.class);
        intent.putExtra(ConstantGlobal.USER_ID, userId);
        intent.putExtra(ConstantGlobal.TRANSACTION_ID, transactionId);
        intent.putExtra("billingEmail", billingEmail);
        intent.putExtra(FIELD_PAYMENT_URL, paymentUrl);
        intent.putExtra("item_sku", sku);
        activity.startActivityForResult(intent, requestCode);
    }

    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode != mRequestCode) {
            return false;
        }
        checkSetupDone("handleActivityResult");

        PaymentResult paymentResult = null;

        if (data == null) {
            logError("Null data in Payment activity result.");
            paymentResult = new PaymentResult();
            if (mOnPaymentFinishedListner != null) {
                mOnPaymentFinishedListner.onPaymentFinished(paymentResult);
            }
            return true;
        }
        paymentResult = (PaymentResult) data.getSerializableExtra("paymentResult");
        if (mOnPaymentFinishedListner != null) {
            mOnPaymentFinishedListner.onPaymentFinished(paymentResult);
        }
        return true;
    }

    void logDebug(String msg) {

        if (mDebugLog)
            Log.d(TAG, msg);
    }

    void logError(String msg) {

        Log.e(TAG, "payment error: " + msg);
    }

    void logWarn(String msg) {

        Log.w(TAG, "payment warning: " + msg);
    }

    public interface OnPaymentFinishedListner {

        public void onPaymentFinished(PaymentResult paymentResult);
    }
}
