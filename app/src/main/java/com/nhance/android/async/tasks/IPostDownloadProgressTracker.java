package com.nhance.android.async.tasks;

import com.nhance.android.db.models.DownloadHistory;

public interface IPostDownloadProgressTracker {

    public void onProgressComplete(DownloadHistory downloadHistory);
}
