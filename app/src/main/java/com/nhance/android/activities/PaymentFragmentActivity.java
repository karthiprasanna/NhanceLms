package com.nhance.android.activities;

import org.apache.commons.lang.StringUtils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nhance.android.activities.baseclasses.NhanceBaseActivity;
import com.nhance.android.billing.payments.PaymentHandler;
import com.nhance.android.billing.payments.PaymentResult;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.jsinterfaces.AbstractJSInterface;
import com.nhance.android.R;

public class PaymentFragmentActivity extends NhanceBaseActivity {

    private static final String TAG                = "PaymentFragmentActivity";
    private static final String PAYMENT_STATUS_URL = "https://payments.nhance.com/mobilePaymentStatus.html";

    private WebView             mWebView;
    private ProgressBar         mProgressBar;
    private Handler             mHandler;
    private MyJSInterface       mJSInterface;

    private String              mUserId;
    private String              mItemSku;
    private String              mTransactionId;
    private String              mPaymentUrl;
    private String              mBillingEmail;

    PaymentResult               paymentResult;

    @SuppressLint({ "SetJavaScriptEnabled", "NewApi" })
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle bundle) {

        super.onCreate(bundle);
        setContentView(R.layout.activity_payment_fragment);
        mHandler = new Handler();
        mUserId = getIntent().getStringExtra(ConstantGlobal.USER_ID);
        mItemSku = getIntent().getStringExtra("item_sku");
        mTransactionId = getIntent().getStringExtra(ConstantGlobal.TRANSACTION_ID);
        mPaymentUrl = getIntent().getStringExtra(PaymentHandler.FIELD_PAYMENT_URL);
        mBillingEmail = getIntent().getStringExtra("billingEmail");

        mWebView = (WebView) findViewById(R.id.payment_window);
        mWebView.getSettings().setJavaScriptEnabled(true);

        mWebView.getSettings().setPluginState(PluginState.ON);
        mWebView.getSettings().setBuiltInZoomControls(true);

        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setAllowFileAccess(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mWebView.getSettings().setAllowFileAccessFromFileURLs(true);
            mWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        }

        mJSInterface = new MyJSInterface();
        mWebView.addJavascriptInterface(mJSInterface, "paymentJSInterface");
        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {

                Log.d(TAG, "onProgressChanged " + System.currentTimeMillis() + " newProgress: "
                        + newProgress);
                if (mProgressBar != null) {
                    mProgressBar.setProgress(newProgress);
                }
                super.onProgressChanged(view, newProgress);
            }

            @Override
            public void onCloseWindow(WebView window) {

                Log.d(TAG, "onCloseWindow: " + window);
                super.onCloseWindow(window);
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {

                Log.d(TAG, "onConsoleMessage: " + consoleMessage.message() + ", line: "
                        + consoleMessage.lineNumber());
                return super.onConsoleMessage(consoleMessage);
            }

        });

        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

                Log.d(TAG, "onPageStarted " + System.currentTimeMillis());
                if (mProgressBar != null) {
                    mProgressBar.setVisibility(View.VISIBLE);
                }
                super.onPageStarted(view, url, favicon);

            }

            @Override
            public void onPageFinished(WebView view, String url) {

                Log.d(TAG, "onPageFinished " + url + " " + System.currentTimeMillis());
                super.onPageFinished(view, url);
                if (mProgressBar != null) {
                    mProgressBar.setVisibility(View.GONE);
                }

            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description,
                    String failingUrl) {

                Log.e(TAG, "onReceivedError errorCode:" + errorCode + ", description:"
                        + description);
                super.onReceivedError(view, errorCode, description, failingUrl);

                String errorMsg = PaymentFragmentActivity.this.getErrorDesc(errorCode);
                mWebView.setVisibility(View.GONE);
                if (mProgressBar != null) {
                    mProgressBar.setVisibility(View.GONE);
                }
                View errorPayment = findViewById(R.id.payment_error_no_internet);
                errorPayment.setVisibility(View.VISIBLE);
                TextView errorText = (TextView) errorPayment.findViewById(R.id.payment_error_text);
                errorText.setText(errorMsg);
                errorPayment.findViewById(R.id.payment_error_close_window_button)
                        .setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {

                                if (mJSInterface != null) {
                                    mJSInterface.closeWindow(StringUtils.EMPTY);
                                } else {
                                    finish();
                                }
                            }
                        });
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {

                Log.e(TAG, "onReceivedSslError" + handler.obtainMessage() + ", error: " + error);
                super.onReceivedSslError(view, handler, error);
            }

        });

        mProgressBar = (ProgressBar) findViewById(R.id.loading_progressbar);
        mWebView.loadUrl(mPaymentUrl + "?userId=" + mUserId + "&email=" + mBillingEmail
                + "&transactionId=" + mTransactionId + "&item_sku=" + mItemSku + "&callbackUrl="
                + PAYMENT_STATUS_URL);
    }

    @Override
    public void onBackPressed() {

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Do you want to cancel The Transaction ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {

                        dialog.cancel();
                        if (mJSInterface != null) {
                            mJSInterface.closeWindow("CANCELLED");
                        }
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                }).create();
        dialog.show();
    }

    @Override
    public void finish() {

        Intent intent = getIntent();
        intent.putExtra("paymentResult", paymentResult);
        setResult(RESULT_OK, intent);
        super.finish();
    }

    class MyJSInterface extends AbstractJSInterface {

        @JavascriptInterface
        public void closeWindow(final String message) {

            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    mWebView.stopLoading();
                    mWebView.loadUrl("about:blank");

                    Log.d(TAG, "closing popup window");

                    paymentResult = new PaymentResult();
                    paymentResult.item_sku = mItemSku;
                    paymentResult.transactionStatus = TextUtils.isEmpty(message) ? PaymentResult.TRANSACTION_STATUS_NOT_STARTED
                            : message;
                    paymentResult.transactionId = mTransactionId;
                    paymentResult.errorMessage = message;
                    PaymentFragmentActivity.this.finish();
                }
            });
        }

        @JavascriptInterface
        public void onPaymentSuccessful(final String transactionId, final String transactionStatus,
                final String item_sku) {

            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    mWebView.stopLoading();
                    mWebView.loadUrl("about:blank");
                    paymentResult = new PaymentResult();
                    paymentResult.item_sku = item_sku;
                    paymentResult.transactionStatus = transactionStatus;
                    paymentResult.transactionId = transactionId;
                    paymentResult.errorMessage = null;
                    PaymentFragmentActivity.this.finish();
                }
            });
        }

        public void setUrlParams(final String params) {

            mHandler.post(new Runnable() {

                @Override
                public void run() {

                    Log.d(TAG, "setUrlParams: " + params);
                    mWebView.loadUrl("javascript:setUrlParams('" + params + "');");
                }
            });
        }
    }

    private String getErrorDesc(int code) {

        String[] network_error_msgs = getResources().getStringArray(
                R.array.error_web_chrome_client_messages);
        int index = Math.abs(code) - 1;
        if (index >= network_error_msgs.length) {
            return "Unknown Error Occured";
        }
        return network_error_msgs[index].split(":")[1];
    }
}
