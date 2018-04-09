package com.nhance.android.async.tasks;

import com.nhance.android.utils.JSONAware;

public interface ICachedTaskProcessor<T extends JSONAware> {

    public void onResultDataFromCache(T data);

    public void onTaskPostExecute(boolean success, T result);
}
