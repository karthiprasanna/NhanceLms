package com.nhance.android.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.nhance.android.managers.FileManager;
import com.nhance.android.managers.StorageHelper;
import com.nhance.android.utils.server.MyNanoHTTPD;

import java.io.File;

public class VServerService extends Service {

    private static final String TAG = "VServerService";
    MyNanoHTTPD                 mServer;

    @Override
    public void onCreate() {

        super.onCreate();
        if (mServer == null) {
            try {
                File lActualFile = null;
                File lFile  = StorageHelper.getStorages(false).get(0).file;
                File lTemp = lFile.getParentFile();
                do{
                    lActualFile = lTemp;
                    lTemp = lActualFile.getParentFile();
                }while (null != lTemp && false == lTemp.getAbsolutePath().equalsIgnoreCase("/"));
                FileManager.MOUNT_PATH = lActualFile.getAbsolutePath();
                mServer = new MyNanoHTTPD(lActualFile);
            } catch (Throwable e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
        new Thread() {

            @Override
            public void run() {

                super.run();
                try {
                    if (!mServer.isAlive()) {
                        mServer.start();
                    }
                } catch (Throwable e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }

        }.start();
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

}
