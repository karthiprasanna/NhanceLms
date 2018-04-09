package com.nhance.android.web;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

public class VedantuWebChromeClient extends WebChromeClient {

    private static final String TAG = "VedantuWebChromeClient";

    @Override
    public boolean onConsoleMessage(ConsoleMessage cm) {

        Log.d(TAG, cm.message() + " -- From line " + cm.lineNumber() + " of " + cm.sourceId());
        return true;
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
        AlertDialog dialog = new AlertDialog.Builder(view.getContext()).
                setMessage(message).
                setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing
                    }
                }).create();
        dialog.show();
        result.confirm();
        return true;
    }
}
