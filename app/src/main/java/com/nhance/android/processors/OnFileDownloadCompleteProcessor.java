package com.nhance.android.processors;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.nhance.android.async.tasks.IDownloadCompleteProcessor;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.datamanagers.DownloadHistoryManager;
import com.nhance.android.db.models.DownloadHistory;
import com.nhance.android.db.models.entity.Content;

public class OnFileDownloadCompleteProcessor implements IDownloadCompleteProcessor<File> {

    private final String TAG = "OnFileDownloadCompleteProcessor";
    private Content      content;
    private Context      context;
    private boolean      isTempStore;

    public OnFileDownloadCompleteProcessor(Content content, Context context, boolean isTempStore) {

        super();
        this.content = content;
        this.context = context;
        this.isTempStore = isTempStore;
    }

    @Override
    public File onComplete(File file, boolean completed) {

        Log.d(TAG, "downloaded file[" + file + "]");
        if (!completed) {
            Log.d(TAG, "download was not successfull");
            return null;
        }

        if (isTempStore) {
            Log.d(TAG, "tempStore hence removing download history for content._id: " + content._id);
            DownloadHistoryManager dhManager = new DownloadHistoryManager(context);
            DownloadHistory dh = dhManager.getDownloadHistory(content._id);
            if (dh != null) {
                dhManager.deleteDownloadHistory(dh._id);
            }
            return file;
        }
        ContentDataManager cDataManager = new ContentDataManager(context);
        content.downloaded = completed;
        ContentValues tobeUpdatedValues = new ContentValues();
        tobeUpdatedValues.put(ConstantGlobal.DOWNLOADED, 1);
        cDataManager.updateContent(content._id, tobeUpdatedValues);
        // File permanentFile = new File(ContentDataManager.getContentDir(content.type),
        // file.getName());
        // if (!permanentFile.getParentFile().exists()) {
        // permanentFile.getParentFile().mkdirs();
        // }
        //
        // boolean deletedTempFile = file.getAbsolutePath().equals(permanentFile.getAbsoluteFile());
        // if (!deletedTempFile) {
        // deletedTempFile = file.renameTo(permanentFile);
        // }
        // Log.d(TAG,
        // "deleted temp file and copied to permanent file : " + deletedTempFile
        // + ", exist permanentFile: " + permanentFile.exists() + " file: "
        // + permanentFile.getAbsolutePath());
        return file;
    }

}
