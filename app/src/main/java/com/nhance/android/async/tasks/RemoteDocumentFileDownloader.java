package com.nhance.android.async.tasks;

import java.io.File;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nhance.android.db.models.entity.Content;
import com.nhance.android.downloader.AbstractFileDownloaderTask;
import com.nhance.android.downloader.DownloadInfo;
import com.nhance.android.downloader.DownloadInfo.DownloadState;
import com.nhance.android.services.FileDownloaderService;

public class RemoteDocumentFileDownloader extends AbstractFileDownloaderTask {

    public ProgressBar               progressBar;
    public TextView                  percentageView;
    IDownloadCompleteProcessor<File> downloadCompleteProcessor;
    ITaskProcessor<File>             postExecuteProcessor;

    public RemoteDocumentFileDownloader(Content content, Context context, String filePath,
            String downloadUrl, ProgressBar progressBar, TextView percentageView,
            ITaskProcessor<File> postExecuteProcessor,
            IDownloadCompleteProcessor<File> downloadCompleteProcessor) {

        super(content, new DownloadInfo(content.orgKeyId, downloadUrl, filePath,
                FileDownloaderService.DOWNLOADER_THREADS), context);

        progressBar.setVisibility(View.VISIBLE);
        percentageView.setVisibility(View.VISIBLE);
        this.progressBar = progressBar;
        this.percentageView = percentageView;
        this.downloadCompleteProcessor = downloadCompleteProcessor;
        this.postExecuteProcessor = postExecuteProcessor;
    }

    @Override
    protected void onPreExecute() {

        super.onPreExecute();
        Log.d("RemoteDocumentFileDownloader", "started task");
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (progressBar != null) {
            this.progressBar.setProgress(values[0].intValue());
        }
        if (percentageView != null) {
            this.percentageView.setText(values[0].intValue() + "%");
        }
    }

    @Override
    protected void onPostExecute(DownloadInfo result) {
        super.onPostExecute(result);

        if (result == null) {
            if (postExecuteProcessor != null) {
                postExecuteProcessor.onTaskPostExecute(false, null);
            }
            return;
        }

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        if (percentageView != null) {
            percentageView.setVisibility(View.GONE);
        }

        if (downloadCompleteProcessor != null) {
            downloadCompleteProcessor.onComplete(new File(result.file),
                    result.state == DownloadState.FINISHED);
        }

        if (postExecuteProcessor != null) {
            postExecuteProcessor.onTaskPostExecute(result.state == DownloadState.FINISHED,
                    new File(result.file));
        }
    }

    @Override
    protected AbstractFileDownloaderTask getResumeDownloaderTask() {
        return null;
    }

    @Override
    protected DownloadState checkDownloadStatus() {
        return null;
    }

}
