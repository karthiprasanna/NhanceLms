package com.nhance.android.downloader;

import java.util.Arrays;

public class DownloadInfo {

    public enum DownloadState {
        TOSTART(0), /* Download has been created but not started yet */
        STARTED(1), /* File is downloading currently */
        PAUSED(2), /* Download has been paused */
        STOPPED(3), /* Download has been canceled */
        FINISHED(4) /* Download has finished properly */;

        private int id;

        DownloadState(int id) {

            this.id = id;
        }

        public int toInt() {

            return id;
        }

        public static DownloadState fromInt(int id) {

            DownloadState[] states = values();
            for (int i = 0; i < states.length; i++)
                if (states[i].id == id)
                    return states[i];
            return null;
        }
    }

    public long                       rowId;            /* ID of the row in the DB */
    public String                     url;
    public String                     file;
    public long                       downloaded, total;
    public long                       created, elapsedTime, lastStarted;
    public int                        threads;
    public DownloadState              state;
    public PartInfo[]                 parts;
    public boolean                    multiPart;
    public int                        orgKeyId;

    public AbstractFileDownloaderTask downloader = null;

    /**
     * This is true if any data has been changed in this download or its parts
     */
    private boolean                   dirty;

    public DownloadInfo(long rowId, int orgKeyId, String url, String file, Integer threads,
            long created, long downloaded, long total, long elapsedTime, int state,
            PartInfo[] parts, boolean multiPart) {

        this.rowId = rowId;
        this.orgKeyId = orgKeyId;
        this.url = url;
        this.file = file;
        this.threads = threads;
        this.created = created;
        this.elapsedTime = elapsedTime;
        this.downloaded = downloaded;
        this.total = total;
        this.state = DownloadState.fromInt(state);
        this.parts = parts;
        this.multiPart = multiPart;
    }

    public DownloadInfo(int orgKeyId, String url, String file, int threads) {

        this(-1, orgKeyId, url, file, threads, System.currentTimeMillis(), 0, 0, 0,
                DownloadState.TOSTART.toInt(), null, false);
    }

    public synchronized void incrementBytes(int bytes) {

        downloaded += bytes;
        // System.out.println("percentage completiong : " + (downloaded * 100) / total);
    }

    public boolean isDirty() {

        return dirty;
    }

    public void setDirty(boolean dirty) {

        this.dirty = dirty;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{rowId:").append(rowId).append(", url:").append(url).append(", file:")
                .append(file).append(", downloaded:").append(downloaded).append(", total:")
                .append(total).append(", created:").append(created).append(", elapsedTime:")
                .append(elapsedTime).append(", lastStarted:").append(lastStarted)
                .append(", threads:").append(threads).append(", state:").append(state)
                .append(", parts:").append(Arrays.toString(parts)).append(", downloader:")
                .append(downloader).append(", dirty:").append(dirty).append("}");
        return builder.toString();
    }

}
