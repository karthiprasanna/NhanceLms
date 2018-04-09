package com.nhance.android.async.tasks;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.nhance.android.activities.AppLandingPageActivity;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.models.DownloadHistory;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.downloader.AbstractFileDownloaderTask;
import com.nhance.android.downloader.DownloadInfo;
import com.nhance.android.downloader.DownloadInfo.DownloadState;

public class VedantuFileDownloaderTask extends AbstractFileDownloaderTask {

    private static final String        TAG                 = "VedantuFileDownloaderTask";
    private NotificationManager        mNotifyManager;
    private NotificationCompat.Builder mBuilder;

    public static final AtomicInteger  ACTIVE_NOTIFICATION = new AtomicInteger(0);

    private int                        notificationId      = 0;
    IDownloadCompleteProcessor<File>   downloadCompleteProcessor;

    public VedantuFileDownloaderTask(Content content, DownloadInfo info, Context context,
            IDownloadCompleteProcessor<File> downloadCompleteProcessor) {

        super(content, info, context);
        if (context == null) {
            return;
        }
        this.downloadCompleteProcessor = downloadCompleteProcessor;
        this.mNotifyManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle(content.name).setContentText("Preparing download...")
                .setSmallIcon(android.R.drawable.stat_sys_download);

        Intent intent = new Intent(context, AppLandingPageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mBuilder.setContentIntent(PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT));

        this.mBuilder = mBuilder;
        Log.i(TAG, "getting notificationId");
        this.notificationId = ACTIVE_NOTIFICATION.incrementAndGet();
        Log.i(TAG, "notificationId : " + this.notificationId);
    }

    @Override
    protected void onPreExecute() {

        super.onPreExecute();
        // Displays the progress bar for the first time.
        Log.d(TAG, "adding progress to notification bar");
        mNotifyManager.notify(notificationId, mBuilder.build());
        Log.d(TAG, "added progress to notification bar");
    }

    @Override
    protected void onProgressUpdate(Integer... values) {

        super.onProgressUpdate(values);
        // publishing the progress....
        // After this onProgressUpdate will be called
        mBuilder.setContentText("" + values[0] + "%");
        mBuilder.setProgress(100, values[0].intValue(), false);
        mNotifyManager.notify(notificationId, mBuilder.build());
    }

    @Override
    protected void onPostExecute(DownloadInfo result) {

        super.onPostExecute(result);
        if (result == null) {
            Log.e(TAG, "error while downloading data from server");
            Toast.makeText(context, "error while downloading data from server", Toast.LENGTH_SHORT).show();
        }
        // When the loop is finished, updates the notification
        if (completed) {
            mBuilder.setContentText("Download complete 100%");
        } else {
            mBuilder.setContentText("Download can not be completed");
        }
        // Removes the progress bar
        mBuilder.setProgress(0, 0, false);
        mNotifyManager.notify(notificationId, mBuilder.build());
        mNotifyManager.cancel(notificationId);
        Log.d(TAG, "Download complete : " + completed);
        ACTIVE_NOTIFICATION.decrementAndGet();
        if (downloadCompleteProcessor != null && result != null) {
            downloadCompleteProcessor.onComplete(new File(result.file), completed);
        }
    }

    @Override
    protected AbstractFileDownloaderTask getResumeDownloaderTask() {

        return new VedantuFileDownloaderTask(content, info, context, downloadCompleteProcessor);
    }

    @Override
    protected DownloadState checkDownloadStatus() {

        DownloadHistory dHistory = dHistoryManager.getDownloadHistory(content._id,
                new String[] { ConstantGlobal.STATUS });
        Log.i(TAG, "checking for download status : " + dHistory);
        if (dHistory == null) {
            this.cancel(true);
            return null;
        }
        info.state = DownloadState.fromInt(dHistory.status);
        if (info.state == DownloadState.STOPPED) {
            Log.i(TAG, "canceling task");
            this.cancel(true);
        }
        return info.state;
    }

    @Override
    protected void onCancelled() {

        super.onCancelled();
        mBuilder.setContentText("Download cancled");
        // Removes the progress bar
        mBuilder.setProgress(0, 0, false);
        mNotifyManager.notify(notificationId, mBuilder.build());
        mNotifyManager.cancel(notificationId);
        Log.d(TAG, "Download complete : " + completed);
        ACTIVE_NOTIFICATION.decrementAndGet();
    }

}
