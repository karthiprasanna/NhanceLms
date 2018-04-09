package com.nhance.android.utils.bookmate;

import android.app.Activity;
import com.google.zxing.integration.android.IntentIntegrator;

public class BookmateUtil {

    public static void scanQRCode(Activity activity) {

        IntentIntegrator integrator = new IntentIntegrator(activity);
        integrator.setSingleTargetApplication(IntentIntegrator.BS_PACKAGE);
        integrator.addExtra("SCAN_MODE", "QR_CODE_MODE");
        integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);
    }
}
