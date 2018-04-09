package com.nhance.android.async.tasks;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public abstract class AbstractVedantuFileDownloader extends AsyncTask<String, Integer, File> {

    private static final String                TAG                = "AbstractVedantuFileDownloader";
    protected ITaskProcessor<File>             postExecuteProcessor;
    protected IDownloadCompleteProcessor<File> downloadCompleteProcessor;
    protected Context                          context;
    protected String                           downloadDir;
    protected String                           url;
    private static final Set<String>           activeDownloadUrls = new HashSet<String>();
    public boolean                             completed;

    // public AbstractVedantuFileDownloader(Context context, String downloadDir) {
    //
    // this(context, downloadDir, null, null);
    // }

    public AbstractVedantuFileDownloader(Context context, String downloadDir,
            ITaskProcessor<File> postExecuteProcessor,
            IDownloadCompleteProcessor<File> downloadCompleteProcessor) {

        super();
        this.context = context;
        this.downloadDir = downloadDir;
        this.postExecuteProcessor = postExecuteProcessor;
        this.downloadCompleteProcessor = downloadCompleteProcessor;
    }

    public void startDownload(String url) {

        this.url = url.trim().toLowerCase();
        boolean added = addDownload(this.url);
        if (added) {
            execute(url);
        } else {
            Toast.makeText(context, "a download is already active for : " + url, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    protected File doInBackground(String... params) {

        String url = params[0];
        Log.d(TAG, "downloading file from url[" + url + "] to : " + downloadDir);

        final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
        final HttpGet getRequest = new HttpGet(url);
        File file = null;

        try {
            HttpResponse response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_PARTIAL_CONTENT) {
                Log.w(TAG, "Error " + statusCode + " while downloading file from " + url);
                return null;
            }

            int count = 0;

            final HttpEntity entity = response.getEntity();
            long dataLength = entity.getContentLength();
            if (entity != null) {
                Log.d(TAG,
                        "is streamed content : " + entity.isStreaming() + ", chunked:"
                                + entity.isChunked());
                InputStream inputStream = null;
                byte data[] = new byte[1024];
                long total = 0;
                // Output stream to write file
                file = new File(downloadDir, StringUtils.substringAfterLast(url, File.separator));
                if (!file.getParentFile().exists()) {
                    file.getParentFile().createNewFile();
                }
                Log.d(TAG, "saving file to : " + file.getAbsolutePath());
                OutputStream output = new FileOutputStream(file);
                try {
                    inputStream = new BufferedInputStream(entity.getContent());
                    int previousPrecentage = 0;
                    while (!isCancelled() && (count = inputStream.read(data)) != -1) {
                        total += count;
                        int percentage = (int) ((total * 100) / dataLength);
                        if (percentage > previousPrecentage) {
                            Log.d(TAG, "total bytes read : " + total + ", percentage: "
                                    + percentage);
                            publishProgress(percentage);
                            previousPrecentage = percentage;
                        }
                        // writing data to file
                        output.write(data, 0, count);
                        completed = percentage == 100;
                    }
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (output != null) {
                        output.flush();
                        output.close();
                    }
                    entity.consumeContent();
                }
            }
        } catch (Exception e) {
            getRequest.abort();
            Log.e(TAG,
                    "Error while downloading file from [" + url + "], errorMessage:"
                            + e.getMessage(), e);
        } finally {
            if (client != null) {
                client.close();
            }
        }
        if (downloadCompleteProcessor != null) {
            return downloadCompleteProcessor.onComplete(file, completed);
        }
        return file;
    }

    @Override
    protected void onCancelled() {

        super.onCancelled();
        Log.d(TAG, "canceling the downloader task");

    }

    @Override
    protected void onPostExecute(File result) {

        boolean downloaded = removeDownload(url) && completed;
        if (postExecuteProcessor != null) {
            postExecuteProcessor.onTaskPostExecute(downloaded, result);
        }
    }

    public static boolean addDownload(String url) {

        return activeDownloadUrls.add(url);
    }

    public static boolean removeDownload(String url) {

        boolean removed = activeDownloadUrls.remove(url);
        Log.d(TAG, "removing active download from task list for url[" + url + "], success: "
                + removed);
        return removed;
    }

}
