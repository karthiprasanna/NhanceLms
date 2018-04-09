package com.nhance.android.downloader;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;

import android.util.Log;

import com.nhance.android.managers.SessionManager;

public class FilePartDownloader implements Runnable {

    public static final int NO_INTERNET_WAIT_TIME = 3000;
    private final String    TAG                   = "FilePartDownloader";
    public PartInfo         info;
    public DownloadInfo     downloadInfo;
    private URL             url;
    private File            file;
    private boolean         isMultiPartSupported;

    public FilePartDownloader(PartInfo info, DownloadInfo downloadInfo, URL url, File file,
            boolean isMultiPartSupported) {

        this.info = info;
        this.downloadInfo = downloadInfo;
        this.url = url;
        this.file = file;
        this.isMultiPartSupported = isMultiPartSupported;
    }

    public URLConnection makeConnection() throws IOException {

        URLConnection connection = null;
        boolean connected = false;
        long startByte = info.firstByte + info.downloaded;

        // Keep retrying until connection is established
        while (!connected && !info.cancelled) {
            // Check network connection first
            // while (!list.isConnected);

            connection = url.openConnection();
            connection.setRequestProperty("User-Agent", AbstractFileDownloaderTask.userAgent);
            connection.setRequestProperty("Range", "bytes=" + startByte + "-" + info.lastByte);
            connection.connect();
            connected = true;
        }

        if (!info.cancelled)
            Log.d(TAG, " Downloading file part " + connection.getContentLength() + " bytes");
        return connection;
    }

    @Override
    public void run() {

        URLConnection connection;

        boolean finished = false;
        InputStream is = null;
        RandomAccessFile raf = null;
        while (!finished && !info.cancelled) {
            try {
                if (!SessionManager.isOnline()) {
                    try {
                        Log.d(TAG, "pausing thread : " + Thread.currentThread().getName());
                        Thread.sleep(NO_INTERNET_WAIT_TIME);
                    } catch (InterruptedException e) {}
                }
                connection = makeConnection();
                long seekTo = info.firstByte + info.downloaded;
                is = connection.getInputStream();
                if (!isMultiPartSupported) {
                    is.skip(seekTo);
                }
                raf = new RandomAccessFile(file, "rw");
                raf.seek(seekTo);

                byte[] b = new byte[1024 * 16];
                int count = 0;
                while (!info.cancelled && (count = is.read(b)) != -1) {
                    if (info.paused) {
                        closeStream(is);
                        closeStream(raf);
                        while (info.paused) {
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {}
                        }
                    }
                    raf.write(b, 0, count);
                    info.downloaded += count;
                    info.setDirty(true);
                    downloadInfo.incrementBytes(count);
                }

                finished = true;

                if (info.cancelled) {
                    Log.e(TAG, info.rowId + " cancelled ");
                }
            } catch (IOException e) {
                Log.e(TAG,
                        "Connection to " + url.getHost() + " broke. Reconnecting... "
                                + e.getMessage());
                try {
                    Thread.sleep(NO_INTERNET_WAIT_TIME);
                } catch (InterruptedException e1) {}
            } finally {
                closeStream(is);
                closeStream(raf);
            }
        }
        Log.d(TAG, "[" + Thread.currentThread().getName() + "] task finished successfully ");
    }

    private void closeStream(Closeable is) {

        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}
