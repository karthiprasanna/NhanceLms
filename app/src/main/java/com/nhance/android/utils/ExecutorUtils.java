package com.nhance.android.utils;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorUtils {

    public static ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 10, 1, TimeUnit.SECONDS,
                                                      new LinkedBlockingQueue<Runnable>());

    // @SuppressLint("NewApi")
    // @SuppressWarnings({ "unchecked", "rawtypes" })
    // public static <T extends AsyncTask, V extends Object> T executeTask(boolean singleThread,
    // T task, V... params) {
    //
    // if (singleThread || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
    // Log.d("ExecutorUtils", "calling execute");
    // task.execute(params);
    // } else {
    // Log.d("ExecutorUtils", "calling executeOnExecutor");
    // task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    // }
    // return task;
    // }
}
