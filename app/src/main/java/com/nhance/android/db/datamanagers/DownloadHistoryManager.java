package com.nhance.android.db.datamanagers;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.models.DownloadHistory;
import com.nhance.android.downloader.DownloadInfo;
import com.nhance.android.downloader.PartInfo;
import com.nhance.android.downloader.DownloadInfo.DownloadState;
import com.nhance.android.utils.SQLDBUtil;

public class DownloadHistoryManager extends AbstractDataManager {

    public static final String TABLE        = "download_history";
    public static final String TABLE_THREAD = "download_thread_info";
    private final String       TAG          = "DownloadHistoryManager";

    public DownloadHistoryManager(Context context) {

        super(context);
    }

    /**
     * @param downloadHistory
     * @throws Exception
     */
    public DownloadHistory upsertDownloadHistory(DownloadHistory downloadHistory) throws Exception {

        DownloadHistory pDHistory = getDownloadHistory(downloadHistory.contentId);
        Log.i(TAG, "inserting into " + TABLE + " table");
        ContentValues values = downloadHistory.toContentValues();
        long id = pDHistory == null ? insert(TABLE, values) : update(TABLE, values, "_id="
                + pDHistory._id, null);
        downloadHistory._id = (int) id;
        return downloadHistory;
    }

    public DownloadHistory getDownloadHistory(int contentId) {

        return getDownloadHistory(contentId, null);
    }

    public DownloadHistory getDownloadHistory(int contentId, String[] fields) {

        DownloadHistory downloadHistory = null;

        StringBuilder sb = new StringBuilder();

        addSelectQuery(sb, TABLE, fields);
        sb.append("WHERE ");
        addIntEqualSQLQuery(ConstantGlobal.CONTENT_ID, contentId, sb);

        Cursor cursor = rawQuery(sb.toString(), null);

        if (cursor != null && cursor.moveToFirst()) {
            downloadHistory = SQLDBUtil.convertToValues(cursor, DownloadHistory.class, fields);
        }

        closeCursor(cursor);
        Log.i(TAG, "returning downloadHistory:" + downloadHistory);

        return downloadHistory;
    }

    /**
     * TODO: remove this method
     * 
     * @param taskId
     * @return
     */

    public DownloadHistory getDownloadHistoryByTaskId(int taskId) {

        DownloadHistory downloadHistory = null;

        StringBuilder sb = new StringBuilder();

        addSelectQuery(sb, TABLE);
        sb.append("WHERE ");
        addIntEqualSQLQuery("taskId", taskId, sb);

        Cursor cursor = rawQuery(sb.toString(), null);

        if (cursor != null && cursor.moveToFirst()) {
            downloadHistory = SQLDBUtil.convertToValues(cursor, DownloadHistory.class, null);
        }
        closeCursor(cursor);
        Log.d(TAG, "returning downloadHistory:" + downloadHistory);

        return downloadHistory;
    }

    public List<DownloadHistory> getPendingDownloads() {

        List<DownloadHistory> list = new ArrayList<DownloadHistory>();

        StringBuilder sb = new StringBuilder();

        addSelectQuery(sb, TABLE);
        sb.append("WHERE ");
        addIntNotEqualSQLQuery(ConstantGlobal.STATUS, DownloadState.FINISHED.toInt(), sb);
        sb.append("AND ");
        addIntNotEqualSQLQuery(ConstantGlobal.STATUS, DownloadState.STOPPED.toInt(), sb);

        Cursor cursor = rawQuery(sb.toString(), null);

        if (cursor != null && cursor.moveToFirst()) {
            DownloadHistory downloadHistory = SQLDBUtil.convertToValues(cursor,
                    DownloadHistory.class, null);
            list.add(downloadHistory);
        }
        closeCursor(cursor);
        Log.d(TAG, "returning pendingDownloads:" + list);

        return list;
    }

    public int updateDownloadHistory(int id, ContentValues tobeUpdatedValues) {

        if (tobeUpdatedValues == null) {
            return -1;
        }
        return update(TABLE, tobeUpdatedValues, "_id=" + id, null);
    }

    public int updateContentDownloadHistory(int contentId, ContentValues tobeUpdatedValues) {

        if (tobeUpdatedValues == null) {
            return -1;
        }
        return update(TABLE, tobeUpdatedValues, "contentId=" + contentId, null);
    }

    public int deleteDownloadHistory(int id) {

        return delete(TABLE_THREAD, KEY_DOWNLOAD_ROWID + "=" + id, null)
                | delete(TABLE, "_id=" + id, null);
    }

    public int updateDownloadHistory(DownloadHistory downloadHistory) {

        ContentValues values = null;
        try {
            values = downloadHistory.toContentValues();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        if (values == null) {
            return -1;
        }
        return update(TABLE, values, "_id=" + downloadHistory._id, null);
    }

    public static void createTable(List<String> createTables) {

        StringBuilder sb = new StringBuilder();

        addCreateTableQuery(sb, TABLE);
        addAbstractAbstractDataModelFeildsRow(sb);
        sb.append(ConstantGlobal.URL).append(" text not null,");
        sb.append(ConstantGlobal.FILE).append(" text not null,");
        sb.append(ConstantGlobal.END_TIME).append(" text,");
        sb.append(KEY_ELAPSED_TIME).append(" text,");
        sb.append(KEY_DOWNLOADED).append(" text,");
        sb.append(KEY_TOTAL).append(" text,");
        sb.append(KEY_THREADS).append(" integer,");
        sb.append(ConstantGlobal.STATUS).append(" integer,");
        sb.append("multiPart").append(" integer,");
        sb.append(ConstantGlobal.CONTENT_ID).append(" integer");

        endCreateTableQuery(sb);
        createTables.add(sb.toString());
        createTables.add(createIndexQuery(TABLE, "download_history_index_content", true,
                ConstantGlobal.CONTENT_ID));
        ThreadsTable.createThreadInfoTable(createTables);
    }

    public synchronized void updateDownloadInfo(int contentId, DownloadInfo info) throws Exception {

        DownloadHistory dHistory = new DownloadHistory(contentId, info);
        long previousRowId = info.rowId;
        info.rowId = upsertDownloadHistory(dHistory)._id;
        if (info.rowId > 0 && info.parts != null) {
            for (PartInfo pInfo : info.parts) {
                if (previousRowId != info.rowId) {
                    insertPartInfo(info, pInfo);
                } else {
                    updatePartInfo(info, pInfo);
                }

            }
        }
    }

    public void insertPartInfo(DownloadInfo info, PartInfo partInfo) {

        ContentValues values = new ContentValues();
        ThreadsTable.populateValues(values, partInfo, info);
        partInfo.rowId = insert(TABLE_THREAD, values);
    }

    public void updatePartInfo(DownloadInfo info, PartInfo partInfo) {

        ContentValues values = new ContentValues();
        ThreadsTable.populateValues(values, partInfo, info);
        update(TABLE_THREAD, values, "_id=" + partInfo.rowId, null);
    }

    public PartInfo[] fetchAllParts(long rowId) {

        Cursor c = rawQuery("select *  from " + TABLE_THREAD + " where " + KEY_DOWNLOAD_ROWID + "="
                + rowId, null);
        if (c.getCount() < 1) {
            closeCursor(c);
            return null;
        }

        int iRowId = c.getColumnIndex(ConstantGlobal._ID);
        int iFirstByte = c.getColumnIndex(KEY_FIRST_BYTE);
        int iLastByte = c.getColumnIndex(KEY_LAST_BYTE);
        int iDownloaded = c.getColumnIndex(KEY_DOWNLOADED);
        int iPaused = c.getColumnIndex(KEY_PAUSED);

        long pRowId, firstByte, lastByte, downloaded;
        boolean paused;

        PartInfo[] parts = new PartInfo[c.getCount()];
        c.moveToFirst();
        for (int i = 0; i < parts.length; i++) {
            pRowId = c.getLong(iRowId);
            firstByte = Long.parseLong(c.getString(iFirstByte));
            lastByte = Long.parseLong(c.getString(iLastByte));
            downloaded = Long.parseLong(c.getString(iDownloaded));
            paused = Boolean.parseBoolean(c.getString(iPaused));
            parts[i] = new PartInfo(pRowId, rowId, firstByte, lastByte, downloaded, paused,
                    downloaded < (lastByte - firstByte));
            c.moveToNext();
        }

        closeCursor(c);
        return parts;
    }

    private static final String KEY_ELAPSED_TIME   = "elapsedTime";
    private static final String KEY_DOWNLOADED     = "downloaded";
    private static final String KEY_TOTAL          = "total";
    private static final String KEY_THREADS        = "threads";
    public static final String  KEY_FIRST_BYTE     = "firstByte";
    public static final String  KEY_LAST_BYTE      = "lastByte";
    public static final String  KEY_PAUSED         = "paused";
    public static final String  KEY_DOWNLOAD_ROWID = "downloadId";

    static class ThreadsTable {

        private static void createThreadInfoTable(List<String> createTables) {

            StringBuilder sb = new StringBuilder();
            addCreateTableQuery(sb, TABLE_THREAD);
            addAbstractAbstractDataModelFeildsRow(sb);
            sb.append(KEY_FIRST_BYTE).append(" text not null,");
            sb.append(KEY_LAST_BYTE).append(" text not null,");
            sb.append(KEY_DOWNLOADED).append(" text,");
            sb.append(KEY_PAUSED).append(" text,");
            sb.append(KEY_DOWNLOAD_ROWID).append(" integer not null,");
            sb.append("FOREIGN KEY(" + KEY_DOWNLOAD_ROWID + ") REFERENCES ").append(TABLE)
                    .append(" (" + ConstantGlobal._ID + ")");

            endCreateTableQuery(sb);
            createTables.add(sb.toString());
        }

        private static void populateValues(ContentValues values, PartInfo part, DownloadInfo info) {

            values.put(KEY_DOWNLOADED, String.valueOf(part.downloaded));
            values.put(KEY_FIRST_BYTE, String.valueOf(part.firstByte));
            values.put(KEY_LAST_BYTE, String.valueOf(part.lastByte));
            values.put(KEY_PAUSED, String.valueOf(part.paused));
            values.put(ConstantGlobal.TIME_CREATED, String.valueOf(part.startTime));
            values.put(KEY_DOWNLOAD_ROWID, info.rowId);

        }
    }

    @Override
    public void createDummyData() {

    }

    @Override
    public void cleanData() {

    }

}
