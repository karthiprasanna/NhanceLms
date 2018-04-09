package com.nhance.android.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.nhance.android.managers.SessionManager;
import com.nhance.android.readers.SDCardReader;

public class SDMountReceiver extends BroadcastReceiver {

    private final String   TAG = "SDMountReceiver";
    private SessionManager session;

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        session = SessionManager.getInstance(context);

        Log.d(TAG, "received mount notification, intent:" + intent);

        if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
            setMounted(context, true, intent);
        } else if (Intent.ACTION_MEDIA_EJECT.equals(action)) {
            setMounted(context, false, intent);
        }

    }

    private void setMounted(Context context, boolean isMounted, Intent intent) {

        if (!session.isLoggedIn()) {
            Log.e(TAG, "no active login, hence not reading SDCard ");
            return;
        }

        SDCardReader sdCardReader = new SDCardReader(context);
        if (isMounted) {
            Log.i(TAG, "looking for nhance package files in : " + intent.getData().getPath());
            sdCardReader.readDataFromSDCard(intent.getData().getPath());
        } else {
            sdCardReader.removeActiveCard();
            sdCardReader.sendBroadCast(false);
        }
    }
}
