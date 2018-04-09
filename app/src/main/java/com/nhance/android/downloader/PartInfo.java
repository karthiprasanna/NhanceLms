package com.nhance.android.downloader;

public class PartInfo {

    public long         rowId, downloadRowId;
    public long         firstByte, lastByte, downloaded;
    public boolean      paused, cancelled;

    public long         total;                          // redundant, used to avoid calculations;

    public DownloadInfo info;
    public long         startTime;

    /**
     * This is true if any data has been changed in this part
     */
    private boolean     dirty;

    public PartInfo(long rowId, long downloadRodId, long firstByte, long lastByte, long downloaded,
            boolean paused, boolean cancelled) {

        this.rowId = rowId;
        this.firstByte = firstByte;
        this.lastByte = lastByte;
        this.downloaded = downloaded;
        this.paused = paused;
        this.cancelled = cancelled;
        this.total = lastByte - firstByte;
        this.info = null;

        dirty = true;
        startTime = System.currentTimeMillis();
    }

    public PartInfo(long firstByte, long lastByte) {

        this(-1, -1, firstByte, lastByte, 0, false, false);
    }

    public boolean isDirty() {

        return dirty;
    }

    public void setDirty(boolean dirty) {

        if (info != null)
            info.setDirty(true);
        this.dirty = dirty;
    }
}
