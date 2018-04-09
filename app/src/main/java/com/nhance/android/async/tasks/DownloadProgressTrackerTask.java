package com.nhance.android.async.tasks;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.datamanagers.DownloadHistoryManager;
import com.nhance.android.db.models.DownloadHistory;
import com.nhance.android.downloader.DownloadInfo.DownloadState;
import com.nhance.android.managers.SessionManager;

public class DownloadProgressTrackerTask extends AsyncTask<Integer, Integer, DownloadHistory> {

    private final String                 TAG       = "DownloadProgressTrackerTask";
    private int                          MAX_TRY   = 20;
    private int                          WAIT_TIME = 3000;
    private TextView                     progressUpdaterView;
    private DownloadHistoryManager       dhHistoryManager;
    private ProgressBar                  progressUpdater;
    private IPostDownloadProgressTracker postDownloadProgressTracker;

    public DownloadProgressTrackerTask(SessionManager session, ProgressBar progressUpdater,
            TextView percentageProgressUpdaterView,
            IPostDownloadProgressTracker postDownloadProgressTracker) {

        this.progressUpdaterView = percentageProgressUpdaterView;
        this.dhHistoryManager = new DownloadHistoryManager(session.getContext());
        this.progressUpdater = progressUpdater;
        this.postDownloadProgressTracker = postDownloadProgressTracker;
        if (progressUpdaterView != null) {
            progressUpdaterView.setText(0 + "% downloaded");
        }
    }

    @Override
    protected DownloadHistory doInBackground(Integer... params) {

        DownloadHistory dHistory = null;
        boolean finished = false;
        int tryCount = 0;
        boolean gotStatus = false;
        while (!finished) {
            if (isCancelled()) {
                return dHistory;
            }

            dHistory = dhHistoryManager.getDownloadHistory(params[0], new String[] {
                    ConstantGlobal.STATUS, ConstantGlobal.DOWNLOADED, "total" });

            gotStatus = dHistory != null;

            if (!gotStatus && tryCount <= MAX_TRY) {
                tryCount++;
                if (tryCount >= MAX_TRY) {
                    return null;
                }
            }

            if (gotStatus) {
                long total = Long.parseLong(dHistory.total);
                int percentage = total < 1 ? 100
                        : (int) ((Long.parseLong(dHistory.downloaded) * 100) / total);

                publishProgress(percentage);

                if (percentage == 100) {
                    return dHistory;
                }

                if (dHistory.status == DownloadState.FINISHED.toInt()
                        || dHistory.status == DownloadState.STOPPED.toInt()
                        || dHistory.status == DownloadState.PAUSED.toInt()) {
                    return dHistory;
                }

            }

            if (isCancelled()) {
                return dHistory;
            }

            try {
                Log.d(TAG, "======== waiting for download status =================");
                Thread.sleep(WAIT_TIME);
                Log.d(TAG, "======== thread woke up ================= finished : " + finished);
            } catch (InterruptedException e) {
                Log.e(TAG, e.getMessage(), e);
            }

        }

        return dHistory;
    }

    @Override
    protected void onPostExecute(DownloadHistory result) {

        super.onPostExecute(result);

        if (result != null && progressUpdaterView != null) {
            if (result.status == DownloadState.PAUSED.toInt()) {
                progressUpdaterView.setText("Download Paused");
            } else if (result.status == DownloadState.FINISHED.toInt()) {
                progressUpdaterView.setText("Download Finished");
            } else if (result.status == DownloadState.STOPPED.toInt()) {
                progressUpdaterView.setText("Download Cancled");
            }
        }
        if (postDownloadProgressTracker != null) {
            postDownloadProgressTracker.onProgressComplete(result);
        }

    }

    @Override
    protected void onProgressUpdate(Integer... values) {

        super.onProgressUpdate(values);
        if (progressUpdater != null && values != null && values[0] != null) {
            Log.d(TAG, "updating progress : " + values[0].intValue());
            progressUpdater.setProgress(values[0].intValue());
            if (progressUpdaterView != null) {
                progressUpdaterView.setText(String.valueOf(values[0]) + "% downloaded");
            }
        }
    }

    @Override
    protected void onCancelled(DownloadHistory result) {

        super.onCancelled(result);
    }

}
