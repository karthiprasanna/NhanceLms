package com.nhance.android.db.helpers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.AnalyticsDataManager;
import com.nhance.android.db.datamanagers.BoardDataManager;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.datamanagers.ContentLinkDataManager;
import com.nhance.android.db.datamanagers.DownloadHistoryManager;
import com.nhance.android.db.datamanagers.ModuleStatusDataManager;
import com.nhance.android.db.datamanagers.OrgDataManager;
import com.nhance.android.db.datamanagers.QuestionAttemptStatusDataManager;
import com.nhance.android.db.datamanagers.SDCardFileMetadataManager;
import com.nhance.android.db.datamanagers.SDCardGroupDataManager;
import com.nhance.android.db.datamanagers.UserActivityDataManager;
import com.nhance.android.db.datamanagers.UserDataManager;

public class VedantuDBHelper extends SQLiteOpenHelper {

    private static final String    TAG              = "VedantuDBHelper";
    public static final String     DATABASE_NAME    = "vedantu_dlp";
    public static final int        DATABASE_VERSION = 1;

    private static VedantuDBHelper mInstance        = null;

    public static VedantuDBHelper getInstance(Context context) {

        if (mInstance == null) {
            mInstance = new VedantuDBHelper(context);
        }
        return mInstance;
    }

    public static void closeDBConntection() {

        if (mInstance != null) {
            mInstance.close();
            mInstance = null;
        }
    }

    private VedantuDBHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        onCreate(getWritableDatabase());
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.d(TAG, "creating required table");
        db.beginTransaction();
        List<String> createTableQuries = new ArrayList<String>();
        ContentDataManager.createTable(createTableQuries);
        BoardDataManager.createTable(createTableQuries);
        ContentLinkDataManager.createTable(createTableQuries);
        AnalyticsDataManager.createTable(createTableQuries);
        OrgDataManager.createTable(createTableQuries);
        UserActivityDataManager.createTable(createTableQuries);
        UserDataManager.createTable(createTableQuries);
        DownloadHistoryManager.createTable(createTableQuries);
        ModuleStatusDataManager.createTable(createTableQuries);
        SDCardGroupDataManager.createTable(createTableQuries);
        SDCardFileMetadataManager.createTable(createTableQuries);
        QuestionAttemptStatusDataManager.createTable(createTableQuries);

        for (String query : createTableQuries) {
            try {
                Log.d(TAG, query);
                db.execSQL(query);
            } catch (Throwable e) {
                // swallow
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        createRequireDirs();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // onCreate(db);
        // Log.d(TAG, "creating required table");
        // db.beginTransaction();
        // List<String> updateTableQuries = new ArrayList<String>();
        // ContentLinkDataManager.updateTable(updateTableQuries);
        // for (String query : updateTableQuries) {
        // try {
        // Log.d(TAG, query);
        // db.execSQL(query);
        // } catch (Throwable e) {
        // // swallow
        // }
        // }
        // db.setTransactionSuccessful();
        // db.endTransaction();
    }

    private void createRequireDirs() {

        File vedantuDir = new File(Environment.getExternalStorageDirectory(), "nhance"
                + File.separator + ConstantGlobal.THUMB);
        if (!vedantuDir.exists()) {
            vedantuDir.mkdirs();
        }
    }

}
