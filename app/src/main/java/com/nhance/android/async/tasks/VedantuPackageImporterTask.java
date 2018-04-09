package com.nhance.android.async.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.pojos.SrcEntity;
import com.nhance.android.utils.IOUtils;
import com.nhance.android.utils.JSONUtils;

public class VedantuPackageImporterTask extends AbstractLibraryLoader {

    private static final String        TAG           = "VedantuPackageImporterTask";
    private static final String        CONTENTS_JSON = "contents.json";
    private NotificationManager        mNotifyManager;
    private NotificationCompat.Builder mBuilder;

    private ZipFile                    zipFile;
    private int                        notificationId;
    private int                        totalCount;

    public VedantuPackageImporterTask(Context context, String file, int totalCount) {

        super(SessionManager.getInstance(context), null, null);
        this.totalCount = totalCount;
        Log.d(TAG, "file[" + file + "] exists : " + new File(file).exists());
        try {
            this.zipFile = new ZipFile(new File(file));
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        this.mNotifyManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle("Importing Package").setContentText("Prepraing import...")
                .setSmallIcon(android.R.drawable.stat_sys_download);
        this.mBuilder = mBuilder;

        Log.i(TAG, "getting notificationId");
        this.notificationId = VedantuFileDownloaderTask.ACTIVE_NOTIFICATION.incrementAndGet();
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
        Log.d(TAG, "updating progress : " + values[0].intValue());
        mBuilder.setContentText("" + values[0] + "/" + totalCount);
        mBuilder.setProgress(totalCount, values[0].intValue(), false);
        mNotifyManager.notify(notificationId, mBuilder.build());
    }

    /**
     * params[0]=sectionId and params[1]=sectionType
     */

    @Override
    protected JSONObject doInBackground(String... params) {

        // read the content of contents.json file
        BufferedReader buffReader = null;
        try {
            ZipEntry contentEntity = zipFile.getEntry(CONTENTS_JSON);
            if (contentEntity == null) {
                contentEntity = zipFile.getEntry(File.separator + CONTENTS_JSON);;
            }
            buffReader = new BufferedReader(new InputStreamReader(
                    zipFile.getInputStream(contentEntity)));
            String readLine = null;
            long latestLibraryContentUpdateTime = 0;
            while ((readLine = buffReader.readLine()) != null) {
                JSONObject linkJSON = new JSONObject(readLine);
                // Log.d(TAG, "content json object : " + readLine);
                // TODO: complete this
                updateLibraryContentLinkData(linkJSON, SyncType.IMPORT, new HashSet<SrcEntity>(), false);
                long contentLastUpdated = JSONUtils.getLong(linkJSON, ConstantGlobal.LAST_UPDATED);
                if (contentLastUpdated > latestLibraryContentUpdateTime) {
                    latestLibraryContentUpdateTime = contentLastUpdated;
                }
                publishProgress(totalContentFetched);
            }

            if (totalContentFetched > 0) {
                session.updateLibraryLatestContentTime(latestLibraryContentUpdateTime, params[0],
                        params[1]);
                long currentTime = System.currentTimeMillis();
                session.updateContentLastSyncTime(currentTime, params[1], params[0]);
                session.updateContentLastSyncTime(currentTime, "OVERALL", "OVERALL");
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        } finally {
            IOUtils.closeStream(buffReader);
        }
        JSONObject res = new JSONObject();
        try {
            res.put("totalParsed", totalContentFetched);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return res;
    }

    @Override
    protected void onPostExecute(JSONObject result) {

        super.onPostExecute(result);
        if (result == null) {
            Log.e(TAG, "error while importing package");
            Toast.makeText(session.getContext(), "error while importing package",
                    Toast.LENGTH_SHORT).show();
        }

        boolean completed = totalContentFetched == totalCount;
        // When the loop is finished, updates the notification
        if (completed) {
            mBuilder.setContentText("Import complete 100%");
        } else {
            mBuilder.setContentText("Import can not be completed");
        }
        // Removes the progress bar
        mBuilder.setProgress(0, 0, false);
        mNotifyManager.notify(notificationId, mBuilder.build());
        mNotifyManager.cancel(notificationId);
        Log.d(TAG, "Download complete : " + completed);
        VedantuFileDownloaderTask.ACTIVE_NOTIFICATION.decrementAndGet();
        if (zipFile != null) {
            try {
                zipFile.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    @Override
    protected boolean downloadImage(String imageUrl, String toDir, String type) {

        String fileName = StringUtils.substringAfterLast(imageUrl, File.separator);
        ZipEntry fileEntry = zipFile.getEntry(type.toLowerCase() + File.separator + fileName);
        if (fileEntry == null) {
            Log.e(TAG, "no fileEntity found for " + type.toLowerCase() + File.separator + fileName);
            return false;
        }
        File outputFile = new File(toDir, fileName);

        if (!outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }
        Log.i(TAG, "copying entity[" + fileEntry + "] to file[" + outputFile + "]");
        InputStream inputStream = null;
        OutputStream output = null;

        int count = 0;
        byte[] data = new byte[1024];
        try {
            output = new FileOutputStream(outputFile);
            inputStream = zipFile.getInputStream(fileEntry);
            while ((count = inputStream.read(data)) != -1) {
                // writing data to file
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            IOUtils.closeStream(inputStream);
            IOUtils.closeStream(output);
        }
        return true;
    }
}
