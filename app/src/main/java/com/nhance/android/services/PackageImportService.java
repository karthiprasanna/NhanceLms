package com.nhance.android.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.nhance.android.async.tasks.VedantuPackageImporterTask;
import com.nhance.android.constants.ConstantGlobal;

public class PackageImportService extends IntentService {

    private final String TAG = "PackageImportService";

    public PackageImportService() {

        super("PackageImportService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String file = intent.getStringExtra(ConstantGlobal.FILE);
        Log.d(TAG, "starting import process for file[" + file + "]");
        VedantuPackageImporterTask exporter = new VedantuPackageImporterTask(
                getApplicationContext(), file, intent.getIntExtra("totalCount", -1));
        exporter.executeTask(false, intent.getStringExtra(ConstantGlobal.ID),
                intent.getStringExtra(ConstantGlobal.TYPE));
    }
}
