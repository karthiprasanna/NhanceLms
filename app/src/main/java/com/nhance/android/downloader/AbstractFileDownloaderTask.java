package com.nhance.android.downloader;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.text.format.Formatter;
import android.util.Log;

import com.nhance.android.async.tasks.AbstractVedantuAsynTask;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.DownloadHistoryManager;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.downloader.DownloadInfo.DownloadState;
import com.nhance.android.managers.SessionManager;
import com.nhance.android.utils.VedantuWebUtils;

public abstract class AbstractFileDownloaderTask extends
        AbstractVedantuAsynTask<Void, Integer, DownloadInfo> {

    private final String               TAG                 = "AbstractFileDownloaderTask";
    public static String               userAgent           = "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.3) Gecko/20100401";

    protected DownloadInfo             info;
    public FilePartDownloader[]        parts;
    public String                      status;
    protected Context                  context;
    protected Content                  content;

    private boolean                    restarting          = false;
    protected boolean                  completed           = false;

    CompletionService<Runnable>        COMPLETION_SERVICE  = new ExecutorCompletionService<Runnable>(
                                                                   new ThreadPoolExecutor(
                                                                           5,
                                                                           128,
                                                                           1,
                                                                           TimeUnit.SECONDS,
                                                                           new LinkedBlockingQueue<Runnable>(
                                                                                   10),
                                                                           sThreadFactory));
    private final int                  MIN_MULTI_PART_SIZE = 1024 * 1024;
    protected DownloadHistoryManager   dHistoryManager     = null;

    private static final ThreadFactory sThreadFactory      = new ThreadFactory() {

                                                               private final AtomicInteger mCount = new AtomicInteger(
                                                                                                          1);

                                                               @Override
                                                            public Thread newThread(Runnable r) {

                                                                   return new Thread(
                                                                           r,
                                                                           "FileDownloader #"
                                                                                   + mCount.getAndIncrement());
                                                               }
                                                           };

    public AbstractFileDownloaderTask(Content content, DownloadInfo info, Context context) {

        this.content = content;
        this.info = info;
        status = "Starting...";
        this.context = context;
        this.dHistoryManager = new DownloadHistoryManager(context);
    }

    private void makeParts() {

        if (info.parts == null) {
            info.parts = new PartInfo[info.threads];
            Log.d(TAG, "no of threads: " + info.threads);
            long bytesPerThread = info.total / info.threads, firstByte = 0, lastByte = 0;

            for (int i = 0; i < info.threads; i++) {
                lastByte = firstByte + bytesPerThread;
                if (i == info.threads - 1) {
                    lastByte = info.total - 1;
                }

                info.parts[i] = new PartInfo(firstByte, lastByte);
                firstByte = lastByte + 1;
            }
        }
    }

    private int getContentLength(URLConnection connection, boolean multiPart) {

        String contentRange = connection.getHeaderField("Content-Range");
        int total = -1;
        try {
            total = Integer.parseInt(StringUtils.substringAfterLast(contentRange, File.separator));
        } catch (Exception e) {
            total = -1;
        }
        if (!multiPart) {
            total = connection.getContentLength();
        }
        return total;
    }

    @Override
    protected DownloadInfo doInBackground(Void... params) {

        Log.d(TAG, "starting download in background");
        if (content == null) {
            Log.e(TAG, "content not found, Stopping the task");
            return null;
        }
        // if (!SessionManager.isOnline()) {
        // Log.e(TAG, "Internet connection is not available");
        // return null;
        // }
        // Check for canelled/finished downloads being loaded from database
        if (info.state == DownloadState.FINISHED || info.state == DownloadState.STOPPED) {
            return info;
        }

        /**
         * Make a connection and partition the download. When recovering from the DB, parts are
         * already existing so we can skip this part TODO: we must check if the file has changed on
         * the server
         */
        if (info.parts == null) {
            if (setStatus("Getting file size...")) {
                return info;
            }

            if (setStatus("Checking multi-part support...")) {
                return info;
            }
            try {
                Log.d(TAG, "getting content length and checking for multipart support");
                URL url = new URL(info.url);
                URLConnection connection = url.openConnection();
                connection.setRequestProperty("Range", "bytes=0-127");
                connection.connect();
                int count = connection.getContentLength();
                info.multiPart = count == 128;

                info.total = getContentLength(connection, info.multiPart);
                if (info.total == -1) {
                    Log.d(TAG, "File size not returned by server");
                    return null;
                }

                if (!info.multiPart) {
                    Log.d(TAG,
                            "Multi-part downloads not supported by server, starting normal download process ");
                    info.threads = 1;
                } else if (info.total <= MIN_MULTI_PART_SIZE) {
                    info.threads = 1;
                }

                return startMultiPartDownload();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
                return null;
            }
        }
        return startMultiPartDownload();
    }

    private DownloadInfo startMultiPartDownload() {
        Log.d(TAG, "File size  " + Formatter.formatFileSize(context, info.total) + " bytes");
        if (setStatus("Creating " + info.file + "...")) {
            return info;
        }

        File file = new File(info.file);
        if (file.exists() && file.length() == info.total) {
            Log.d(TAG,
                    "file already downloaded locally on some other library, hecn just marking it downloaded");
            info.downloaded = info.total;
            makeParts();
            for (PartInfo pInfo : info.parts) {
                pInfo.downloaded = pInfo.total;
            }
            markDownloadComplete();
            return info;
        }
        try {
            createEmptyFile(file, info.total);
        } catch (IOException e) {
            Log.e(TAG,
                    "Could not create output file. Check space and permissions. " + e.getMessage(),
                    e);
            return null;
        }

        if (info.state == DownloadState.TOSTART || info.state == DownloadState.STARTED) {
            info.state = DownloadState.STARTED;
            info.lastStarted = System.currentTimeMillis();
        }

        makeParts();
        URL url;
        try {
            url = new URL(info.url);
        } catch (MalformedURLException e1) {
            return null;
        }
        parts = new FilePartDownloader[info.threads];
        for (int i = 0; i < info.threads; i++) {
            parts[i] = new FilePartDownloader(info.parts[i], info, url, file, info.multiPart);
            COMPLETION_SERVICE.submit(parts[i], null);
        }

        updateDownloadHistory(false);

        int previousPrecentage = 0;
        while (info.downloaded < info.total && info.state != null
                && (info.state != DownloadState.FINISHED && info.state != DownloadState.STOPPED)) {
            boolean online = false;
            if (!SessionManager.isOnline() && !(online = VedantuWebUtils.isOnline(context))) {
                try {
                    Log.d(TAG, "pausing [info.state: " + info.state + "] isOnline: "
                            + SessionManager.isOnline());
                    Thread.sleep(FilePartDownloader.NO_INTERNET_WAIT_TIME);
                } catch (InterruptedException e) {}
            }
            if (online) {
                SessionManager.setOnline(online);
            }

            if (info.state == DownloadState.PAUSED) {
                if (setStatus("Download paused "
                        + Formatter.formatFileSize(context, info.downloaded) + "/"
                        + Formatter.formatFileSize(context, info.total))) {
                    return info;
                }
                while (info.state == DownloadState.PAUSED) {
                    try {
                        Log.d(TAG, "pausing [info.state: " + info.state + "]");
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {}
                }
            } else {
                int percentage = (int) ((info.downloaded * 100) / info.total);
                if (percentage > previousPrecentage) {
                    Log.d(TAG, "total bytes read : " + info.downloaded + "/" + info.total
                            + ", percentage: " + percentage);
                    publishProgress(percentage);
                    if (percentage % 5 == 0) {
                        updateDownloadHistory(true);
                    }
                    previousPrecentage = percentage;
                }

                // if (setStatus(Formatter.formatFileSize(context, info.downloaded) + "/"
                // + Formatter.formatFileSize(context, info.total))) {
                // return info;
                // }
                if (setStatus("Downloaded  " + info.downloaded + "/ " + info.total)) {
                    return info;
                }
            }
            long timePassed = System.currentTimeMillis() - info.lastStarted;
            if (timePassed % 3000 == 0) {
                checkDownloadStatus();
            }

        }
        markDownloadComplete();
        if (isCancelled()) {
            setStatus("Canceled");
        }
        return info;
    }

    private void markDownloadComplete() {

        completed = info.downloaded == info.total;
        if (completed) {
            publishProgress(100);
        }

        if (info.state == DownloadState.STARTED && completed) {
            info.elapsedTime += System.currentTimeMillis() - info.lastStarted;
            info.lastStarted = 0;
            info.state = DownloadState.FINISHED;
        }
    }

    @Override
    protected void onPostExecute(DownloadInfo result) {

        if (info.downloaded >= info.total) {
            Log.w(TAG,
                    "File " + info.file + " download["
                            + Formatter.formatFileSize(context, info.downloaded) + "], total["
                            + Formatter.formatFileSize(context, info.total) + "] finished ");
            setStatus(Formatter.formatFileSize(context, info.total) + " downloaded ");
            info.state = DownloadState.FINISHED;
        } else {
            Log.w(TAG, "File " + info.file + " download cancelled");
            setStatus("Canceled");
            info.state = DownloadState.STOPPED;
        }
        if (info.total > 0) {
            updateDownloadHistory(false);
        }
    }

    private void updateDownloadHistory(boolean partialUpdate) {

        try {
            if (partialUpdate) {
                ContentValues values = new ContentValues();
                values.put(ConstantGlobal.DOWNLOADED, String.valueOf(info.downloaded));
                dHistoryManager.updateDownloadHistory((int) info.rowId, values);
            } else {
                dHistoryManager.updateDownloadInfo(content._id, info);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /*
     * Utility methods
     */
    boolean pauseDownload() {

        if (info.state == DownloadState.STARTED) {
            if (parts != null) {
                for (int i = 0; i < parts.length; i++)
                    parts[i].info.paused = true;
            }
            info.state = DownloadState.PAUSED;
            info.elapsedTime = getTotalTime();
            setStatus("Pausing...");
            return true;
        }
        return false;
    }

    private static void createEmptyFile(File file, long size) throws IOException {

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        try {
            raf.setLength(size);
        } finally {
            raf.close();
        }
    }

    private long getTotalTime() {

        long total = info.elapsedTime;
        if (info.state == DownloadState.STARTED)
            total += System.currentTimeMillis() - info.lastStarted;
        return total;
    }

    private void startNewDownloader() {

        setStatus("Restarting...");
        info.state = DownloadState.TOSTART;
        info.downloaded = 0;
        info.elapsedTime = 0;
        info.parts = null;
        info.downloader = getResumeDownloaderTask();
        info.downloader.executeTask(false);
    }

    void restartDownload() {

        restarting = true;
        if (!cancel(false)) {
            startNewDownloader();
        }
    }

    @Override
    protected void onCancelled() {

        Log.d(TAG, "onCancelled called");
        super.onCancelled();
        if (parts != null) {
            for (int i = 0; i < parts.length; i++) {
                if (parts[i] != null) {
                    parts[i].info.cancelled = true;
                    parts[i].info.paused = false;
                }
            }
        }
        Log.w(TAG, "File " + info.file + " download cancelled");
        info.elapsedTime = getTotalTime();
        info.state = DownloadState.STOPPED;
        setStatus("Canceled");
        updateDownloadHistory(false);
        if (restarting) {
            startNewDownloader();
        } else {
            try {
                new File(info.file).delete();
            } catch (Exception e) {}
            dHistoryManager.deleteDownloadHistory((int) info.rowId);
        }
    }

    private boolean setStatus(String str) {

        // Log.d(TAG, "SETTING STATUS: " + str);
        info.setDirty(true);
        if (isCancelled()) {
            status = "Cancelled";
            info.state = DownloadState.STOPPED;
            return true;
        }
        status = str;
        return false;
    }

    @Override
    @SuppressLint("NewApi")
    public AbstractFileDownloaderTask executeTask(boolean singleThread, Void... params) {

        if (singleThread || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            Log.d("ExecutorUtils", "calling execute");
            execute(params);
        } else {
            Log.d("ExecutorUtils", "calling executeOnExecutor");
            executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        }
        return this;
    }

    protected abstract AbstractFileDownloaderTask getResumeDownloaderTask();

    protected abstract DownloadState checkDownloadStatus();

}
