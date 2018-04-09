package com.nhance.android.async.tasks;

public interface IDownloadCompleteProcessor<T> {

    /**
     * @param result
     *            --> downloaded file
     * @param completed
     *            --> was the file completely downloaded
     * @return <li>
     *         <p>
     *         return the final file that will be used by the requester, if the download was
     *         unsuccessful then the temp file will be remove and null will be returned
     *         </p>
     *         </li>
     */
    public T onComplete(T result, boolean completed);
}
