package com.nhance.android.services;

import java.io.File;
import java.util.List;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.nhance.android.assignment.async.tasks.AssignmentDownloader;
import com.nhance.android.assignment.processors.analytics.OnAssignmentDownloadCompleteProcessor;
import com.nhance.android.async.tasks.TestDownloader;
import com.nhance.android.async.tasks.VedantuFileDownloaderTask;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.datamanagers.DownloadHistoryManager;
import com.nhance.android.db.models.DownloadHistory;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.downloader.DownloadInfo;
import com.nhance.android.downloader.DownloadInfo.DownloadState;
import com.nhance.android.enums.EntityType;
import com.nhance.android.processors.OnFileDownloadCompleteProcessor;
import com.nhance.android.processors.OnTestDownloadCompleteProcessor;

public class FileDownloaderService extends IntentService {

    private String                 TAG                = "FileDownloaderService";
    public static final int        DOWNLOADER_THREADS = 2;
    private DownloadHistoryManager mDowmHistoryManager;
    private ContentDataManager     mContentDataManager;

    public FileDownloaderService() {

        super("FileDownloaderService");
    }

    public class FileDownloadBinder extends Binder {

        FileDownloaderService getService() {

            return FileDownloaderService.this;
        }

    }

    private final IBinder mBinder = new FileDownloadBinder();

    @Override
    public void onCreate() {

        super.onCreate();
        mDowmHistoryManager = new DownloadHistoryManager(getApplicationContext());
        mContentDataManager = new ContentDataManager(getApplicationContext());
        // start the pending download here
        List<DownloadHistory> pendingDownloads = mDowmHistoryManager.getPendingDownloads();
        for (DownloadHistory dHistory : pendingDownloads) {
            Content content = mContentDataManager.getContent(dHistory.contentId);
            EntityType entityType = EntityType.valueOfKey(content.type);
            boolean download = entityType == EntityType.TEST
                    || entityType == EntityType.ASSIGNMENT;
            addDownload(content, dHistory, dHistory.url, dHistory.file, download,entityType);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {

        return mBinder;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // String url = "http://video.ted.com/talk/podcast/2013G/None/EricXLi_2013G-light.mp4";

        String file = intent.getStringExtra(ConstantGlobal.FILE);
        String url = intent.getStringExtra(ConstantGlobal.URL);
        int contentId = intent.getIntExtra(ConstantGlobal.CONTENT_ID, -1);
        Log.d(TAG, "intentExtras: " + intent.getExtras());
        if (contentId == -1) {
            return;
        }

        Content content = mContentDataManager.getContent(contentId);

        if (content == null || content.downloaded) {
            return;
        }
        EntityType entityType = EntityType.valueOfKey(content.type);
        boolean download = entityType == EntityType.TEST || entityType == EntityType.ASSIGNMENT;
        if (!download && (TextUtils.isEmpty(url) || TextUtils.isEmpty(file))) {
            return;
        }

        DownloadHistory dHistory = mDowmHistoryManager.getDownloadHistory(contentId);
        addDownload(content, dHistory, url, file, download,entityType);
    }

    private void addDownload(Content content, DownloadHistory dHistory, String url, String file,
                             boolean download,EntityType entityType) {
        if (dHistory != null && !download) {
            Log.d(TAG, "a download already in progress or completed");
            File downloadedFile = new File(dHistory.file);
            DownloadState dState = DownloadState.fromInt(dHistory.status);
            if (dState != null && dState != DownloadState.STOPPED && downloadedFile.exists()
                    && downloadedFile.length() == Long.valueOf(dHistory.total)) {
                Log.d(TAG, "download for content[" + content._id + "] already present with state["
                        + dState + "] file: " + downloadedFile.getAbsolutePath());
                ContentValues cValues = new ContentValues();
                cValues.put(ConstantGlobal.DOWNLOADED, 1);
                mContentDataManager.updateContent(content._id, cValues);
                return;
            }
        }

        DownloadInfo dInfo = new DownloadInfo(content._id, url, file, DOWNLOADER_THREADS);

        OnFileDownloadCompleteProcessor onFileDownloadCompleteProcessor = new OnFileDownloadCompleteProcessor(
                content, getApplicationContext(), false);
        //Himank
        if (download) {
            if(entityType == EntityType.TEST) {

                TestDownloader downloader = new TestDownloader(content, getApplicationContext(),
                        new OnTestDownloadCompleteProcessor(content, getApplicationContext()));
                Log.e(TAG, "starting test download contentId: " + content._id);
                downloader.executeTask(false);
                return;
            }else if (entityType == EntityType.ASSIGNMENT){
                AssignmentDownloader downloader = new AssignmentDownloader(content, getApplicationContext(),
                        new OnAssignmentDownloadCompleteProcessor(content, getApplicationContext()));
                Log.e(TAG, "starting assessment download contentId: " + content._id);
                downloader.executeTask(false);
                return;
            }
        }
        VedantuFileDownloaderTask downloader = new VedantuFileDownloaderTask(content, dInfo,
                getApplicationContext(), onFileDownloadCompleteProcessor);
        Log.e(TAG, "starting download");
        downloader.executeTask(false);
    }
}
