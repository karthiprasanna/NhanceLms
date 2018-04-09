package com.nhance.android.activities;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

import com.google.zxing.Result;
import com.nhance.android.R;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.datamanagers.ContentLinkDataManager;
import com.nhance.android.db.models.entity.ContentLink;
import com.nhance.android.enums.EntityType;
import com.nhance.android.library.utils.LibraryUtils;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.LibraryContentRes;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QrCodeScannerActivity extends AppCompatActivity
        implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;

    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Programmatically initialize the scanner view
        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);

        sessionManager = SessionManager.getInstance(getApplicationContext());
    }

    @Override
    public void handleResult(Result rawResult) {



        String invalidContentMsg = "OOPs! Invalid V(QR) Code :(";
        String[] values = TextUtils.isEmpty(rawResult.getText()) ? null : TextUtils.split(rawResult.getText(), "/");
        boolean error = values == null || values.length < 5; // /org/oggId/entityType/entityId
        if (error) {
            Toast.makeText(this, invalidContentMsg, Toast.LENGTH_LONG).show();
            return;
        }

        if (!TextUtils.equals(values[2],
                sessionManager.getSessionStringValue(ConstantGlobal.ORG_ID))) {
            Toast.makeText(this, "OOPs! Content does not belong to app publisher :(",
                    Toast.LENGTH_LONG).show();
            return;
        }
        ContentLinkDataManager linkDataManager = new ContentLinkDataManager(
                getApplicationContext());
        ContentLink contentLink = linkDataManager.getContentLink(values[4], EntityType
                .valueOfKey(values[3]).name(), sessionManager
                .getSessionStringValue(ConstantGlobal.USER_ID), null, null, sessionManager
                .getSessionIntValue(ConstantGlobal.ORG_KEY_ID));
        error = contentLink == null;
        if (error) {
            Toast.makeText(
                    this,
                    "OOPs! Seem content is not available locally, sync your library to get latest content :(",
                    Toast.LENGTH_LONG).show();
            return;
        }
        LibraryContentRes libraryContentRes = new ContentDataManager(
                getApplicationContext()).getLibraryContentRes(contentLink.linkId);
        if (libraryContentRes != null) {
            LibraryUtils.onLibraryItemClickListnerImpl(getApplicationContext(),
                    libraryContentRes);
        } else {
            Toast.makeText(this, invalidContentMsg, Toast.LENGTH_LONG).show();
            return;
        }
        // Do something with the result here
     /*   Log.v("TAG", rawResult.getText()); // Prints scan results
        // Prints the scan format (qrcode, pdf417 etc.)
        Log.v("TAG", rawResult.getBarcodeFormat().toString());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scan Result");
        builder.setMessage(rawResult.getText());
        AlertDialog alert1 = builder.create();
        alert1.show();*/

        // If you would like to resume scanning, call this method below:
        //mScannerView.resumeCameraPreview(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register ourselves as a handler for scan results.
        mScannerView.setResultHandler(this);
        // Start camera on resume
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop camera on pause
        mScannerView.stopCamera();
    }
}