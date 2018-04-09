package com.nhance.android.async.tasks;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

public abstract class AbstractVedantuAsynTask<Params, Progress, Result> extends
        AsyncTask<Params, Progress, Result> {

    @SuppressLint("NewApi")
    public AbstractVedantuAsynTask<Params, Progress, Result> executeTask(boolean singleThread,
            Params... params) {

        try {
             if (singleThread || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                Log.v("AbstractVAsyncTask", "calling execute");
                execute(params);
            } else {
                Log.v("AbstractVAsyncTask", "calling executeOnExecutor");
                executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
            }
        } catch (Throwable t) {
            Log.e("AbstractVedantuAsynTask", t.getMessage(), t);
        }
        return this;
    }
}
