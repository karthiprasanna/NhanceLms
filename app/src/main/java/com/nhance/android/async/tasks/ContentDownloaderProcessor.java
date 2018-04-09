package com.nhance.android.async.tasks;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import android.app.DownloadManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.ContentDataManager;
import com.nhance.android.db.datamanagers.DownloadHistoryManager;
import com.nhance.android.db.models.DownloadHistory;
import com.nhance.android.pojos.LibraryContentRes;
import com.nhance.android.services.FileDownloaderService;

public class ContentDownloaderProcessor extends AbstractDownloadProcessor {

    private static String TAG = "ContentDownloaderProcessor";

    public ContentDownloaderProcessor(Context context, LibraryContentRes content) {

        super(context, content);
    }

    @Override
    protected void setUpDownloaderTask(boolean skipUrlCheck, String downloadUrl) {

        if (!skipUrlCheck && StringUtils.isEmpty(downloadUrl)) {
            Toast.makeText(context, "downloading not allowed : " + errorMsg, Toast.LENGTH_LONG)
                    .show();
            return;
        }

        Log.d(TAG, "downloadUrl : " + downloadUrl);

        String filePath = TextUtils.join(File.separator,
                new String[] { ContentDataManager.getContentDir(content.type.toLowerCase()),
                        StringUtils.substringAfterLast(downloadUrl, File.separator) });

        Intent intent = new Intent(context, FileDownloaderService.class);
        intent.putExtra(ConstantGlobal.URL, downloadUrl);
        intent.putExtra(ConstantGlobal.FILE, filePath);
        intent.putExtra(ConstantGlobal.CONTENT_ID, content._id);
        context.startService(intent);
        // dTask = new FileDownloaderWithNotification(context,
        // ContentDataManager.getTempContentDir(),
        // this, downloadCompleteProcessor);
        // dTask.startDownload(downloadUrl);

        // setUpUsingDefaultDownloader(downloadUrl);
    }

    private void setUpUsingDefaultDownloader(String downloadUrl) {

        if (StringUtils.isEmpty(downloadUrl)) {
            Toast.makeText(context, "downloading not allowed", Toast.LENGTH_SHORT).show();
            return;
        }
        DownloadHistoryManager dHManager = new DownloadHistoryManager(context);
        DownloadHistory dHistory = dHManager.getDownloadHistory(content._id);
        if (dHistory != null && (dHistory.status == DownloadManager.STATUS_RUNNING)) {
            Log.w(TAG, "a download is already running");
            Toast.makeText(context, "a download is already in progress", Toast.LENGTH_SHORT).show();
            return;
        }

        DownloadManager downloadManager = (DownloadManager) context
                .getSystemService(Context.DOWNLOAD_SERVICE);
        // downloadUrl =
        // "http://s3.amazonaws.com/video-dev-vedantu/6c661d01-93db-4d02-a6da-ee75fd2ba137.vid.vid.orig.mp4?Expires=1373533823&AWSAccessKeyId=AKIAJKU2GBHZNXI2OZOQ&Signature=Oa9Z2uITCTPxSkV982jloiEEwaI%3D";
        // "http://imgdev.vedantu.com/viewer/view/video/vid/6c661d01-93db-4d02-a6da-ee75fd2ba137.vid.vid.orig.mp4";

        Log.d(TAG, "downloadUrl : " + downloadUrl);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
        // "http://192.168.1.100:19019/viewer/view/video/vid/4c2022e7-39cb-4e5f-904a-fdded87dadd0.vid.vid.orig.mp4"

        request.setTitle(content.name).setDescription("Download In Progress... ");

        String filePath = TextUtils.join(
                File.separator,
                new String[] { Environment.getExternalStorageDirectory().getAbsolutePath(),
                        "nhance", content.type.toLowerCase(),
                        StringUtils.substringAfterLast(downloadUrl, File.separator) });
        File diskFile = new File(filePath);
        if (!diskFile.getParentFile().exists()) {
            diskFile.getParentFile().mkdirs();
        }
        if (dHistory != null && diskFile.exists()) {
            Log.d(TAG, "file already downoaded");
            dHistory.status = DownloadManager.STATUS_SUCCESSFUL;
            ContentValues cValues = new ContentValues();
            cValues.put(ConstantGlobal.DOWNLOADED, 1);
            new ContentDataManager(context).updateContent(content._id, cValues);
            Toast.makeText(context, "file already downoaded", Toast.LENGTH_SHORT).show();
            return;
        }
        request.setDestinationUri(Uri.fromFile(diskFile));
        Log.d(TAG, "starting download for : " + downloadUrl);
        if (dHistory == null) {
            dHistory = new DownloadHistory(content.orgKeyId, downloadUrl, filePath, content._id,
                    String.valueOf(0));
        }

        dHistory.status = DownloadManager.STATUS_RUNNING;
        dHistory.file = filePath;
        dHistory.url = downloadUrl;

        // registerBroadcastReceiver();

        long taskId = downloadManager.enqueue(request);

        // dHistory.taskId = (int) taskId;

        try {
            new DownloadHistoryManager(context).upsertDownloadHistory(dHistory);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

    }

    // private void registerBroadcastReceiver() {
    //
    // IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
    // List<ResolveInfo> resolveInfo = context.getPackageManager().queryBroadcastReceivers(
    // new Intent(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
    // PackageManager.GET_INTENT_FILTERS);
    // if (resolveInfo.isEmpty()) {
    // Log.w(TAG, "registering broadcast receiver for INTENT_FILTER :"
    // + DownloadManager.ACTION_DOWNLOAD_COMPLETE);
    // context.registerReceiver(new ContentDownloadReceiver(), intentFilter);
    // } else {
    // Log.w(TAG, "a broadCast receiver already registred for INTENT_FILTER :"
    // + DownloadManager.ACTION_DOWNLOAD_COMPLETE);
    // }
    // }

    @Override
    public void onTaskStart(File result) {

    }

}
