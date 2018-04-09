package com.nhance.android.db.models;

import android.content.Context;

import com.nhance.android.db.datamanagers.DownloadHistoryManager;
import com.nhance.android.downloader.DownloadInfo;

public class DownloadHistory extends AbstractDataModel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public String             url;
    public String             file;
    public int                contentId;
    public int                status;
    public int                threads;              // how many threads are used to download this
                                                     // file
    public String             endTime;
    public String             elapsedTime;
    public String             downloaded;           // downloaded bytes so far
    public String             total;                // total bytes to be downloaded
    public boolean            multiPart;

    public DownloadHistory() {

        super();
    }

    public DownloadHistory(String timeCreated, int orgKeyId) {

        super(timeCreated, orgKeyId);
    }

    public DownloadHistory(int orgKeyId, String url, String file, int contentId, String endTime) {

        super(String.valueOf(System.currentTimeMillis()), orgKeyId);
        this.url = url;
        this.file = file;
        this.contentId = contentId;
        this.endTime = endTime;
    }

    public DownloadHistory(int contentId, DownloadInfo dInfo) {

        super(String.valueOf(dInfo.created), dInfo.orgKeyId);
        this.contentId = contentId;
        this.downloaded = String.valueOf(dInfo.downloaded);
        this.elapsedTime = String.valueOf(dInfo.elapsedTime);
        this.file = dInfo.file;
        this.status = dInfo.state.toInt();
        this.threads = dInfo.threads;
        this.total = String.valueOf(dInfo.total);
        this.url = dInfo.url;
    }

    public DownloadInfo toDownloadInfo(Context context, boolean addPartsInfo) {

        DownloadInfo dInfo = new DownloadInfo(_id, orgKeyId, url, file, threads,
                Long.valueOf(timeCreated), Long.valueOf(downloaded), Long.valueOf(total),
                Long.valueOf(elapsedTime), status, null, multiPart);
        if (addPartsInfo) {
            dInfo.parts = new DownloadHistoryManager(context).fetchAllParts(_id);
        }
        return dInfo;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{url:").append(url).append(", file:").append(file).append(", contentId:")
                .append(contentId).append(", status:").append(status).append(", threads:")
                .append(threads).append(", endTime:").append(endTime).append(", elapsedTime:")
                .append(elapsedTime).append(", downloaded:").append(downloaded).append(", total:")
                .append(total).append(", _id:").append(_id).append(", orgKeyId:").append(orgKeyId)
                .append(", timeCreated:").append(timeCreated).append("}");
        return builder.toString();
    }

}
