package com.nhance.android.async.tasks;

public interface ITaskProcessor<T> {

    /**
     * this method will be called in background thread after fetching data from server
     * 
     * @param result
     */
    public void onTaskStart(T result);

    /**
     * this method will be called on postExecute method of AsyncTask in the UI Thread
     * 
     * @param success
     *            : return true if the task completed successfully else false.
     * 
     */
    public void onTaskPostExecute(boolean success, T result);
}
