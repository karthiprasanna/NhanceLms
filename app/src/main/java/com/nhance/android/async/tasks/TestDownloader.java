package com.nhance.android.async.tasks;

import org.json.JSONObject;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.nhance.android.activities.AppLandingPageActivity;
import com.nhance.android.db.models.entity.Content;

/**
 * 
 * @author shankar NOTE: this will be used to download test questions
 */
public class TestDownloader extends AbstractTestDownloader {

    private final String                   TAG                 = "TestDownloader";

    private NotificationManager            mNotifyManager;
    private NotificationCompat.Builder     mBuilder;

    private int                            notificationId      = 0;
    IDownloadCompleteProcessor<JSONObject> downloadCompleteProcessor;
    private int                            completedPercentage = 0;

    public TestDownloader(Content content, Context context,
            IDownloadCompleteProcessor<JSONObject> downloadCompleteProcessor) {

        super(content, context);
        this.downloadCompleteProcessor = downloadCompleteProcessor;
        this.mNotifyManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle(content.name).setContentText("Prepraing download...")
                .setSmallIcon(android.R.drawable.stat_sys_download);

        Intent intent = new Intent(context, AppLandingPageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mBuilder.setContentIntent(PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT));

        this.mBuilder = mBuilder;
        Log.d(TAG, "getting notificationId");
        this.notificationId = VedantuFileDownloaderTask.ACTIVE_NOTIFICATION.incrementAndGet();
        Log.d(TAG, "notificationId : " + notificationId);
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
        completedPercentage = values[0].intValue();
        mBuilder.setContentText("" + completedPercentage + "%");
        mBuilder.setProgress(100, completedPercentage, false);
        mNotifyManager.notify(notificationId, mBuilder.build());
    }

    @Override
    protected void onPostExecute(JSONObject result) {

        super.onPostExecute(result);
        if (result == null) {
            Log.e(TAG, "error while downloading data from server");
            Toast.makeText(context, "error while downloading data from server", Toast.LENGTH_SHORT)
                    .show();
        }
        boolean completed = completedPercentage == 100;
        // Removes the progress bar
        mBuilder.setProgress(0, 0, false);
        mNotifyManager.notify(notificationId, mBuilder.build());
        mNotifyManager.cancel(notificationId);
        VedantuFileDownloaderTask.ACTIVE_NOTIFICATION.decrementAndGet();
        if (downloadCompleteProcessor != null && result != null) {
            downloadCompleteProcessor.onComplete(result, completed);
        }
    }

}
