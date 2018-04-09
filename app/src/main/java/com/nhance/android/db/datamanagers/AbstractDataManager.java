package com.nhance.android.db.datamanagers;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.nhance.android.assignment.db.models.analytics.AssignmentAnalytics;
import com.nhance.android.assignment.receiver.AssignmentNetworkMonitor;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.helpers.VedantuDBHelper;
import com.nhance.android.managers.LocalManager;
import com.nhance.android.receivers.NetworkMonitor;
import com.nhance.android.utils.SQLDBUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.nhance.android.db.datamanagers.AnalyticsDataManager.TABLE_ENTITY_ANALYTICS;

public abstract class AbstractDataManager {

    /**
     *
     */
    protected static final String SPACE             = " ";
    protected static final String SQL_KEYWORD_AND   = " AND ";
    protected static final String SQL_KEYWORD_WHERE = " WHERE ";

    private SQLiteDatabase        dbWrite;
    private SQLiteDatabase        dbRead;
    private VedantuDBHelper       dbHelper;
    private static final String   TAG               = "AbstractDataManager";
    private Context               context;

    /**
     * pass this constructor a applicationContext
     **/

    public AbstractDataManager(Context context) {

        //

        dbHelper = VedantuDBHelper.getInstance(context);
        this.context = context;
    }

    public Context getContext() {

        return context;
    }

    protected void openWritable() {

        dbWrite = dbHelper.getWritableDatabase();
    }

    protected void openReadable() {

        dbRead = dbHelper.getReadableDatabase();
    }

    public Cursor rawQuery(String query, String[] selectionArgs) {

        if (dbRead == null) {
            openReadable();
        }
        Log.v(TAG, "sqlQuery : " + query + ", selectionArgs: " + selectionArgs);
        return dbRead.rawQuery(query, selectionArgs);
    }

    public Cursor query(String table, String orderBy, String limit) {

        return query(table, null, orderBy, limit);
    }

    public Cursor query(String table, String[] fields, String orderBy, String limit) {

        return query(table, fields, null, null, null, null, orderBy, limit);
    }

    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs,
                        String groupBy, String having, String orderBy, String limit) {

        if (dbRead == null) {
            openReadable();
        }
        Log.v(TAG, "query for table: " + table);
        return dbRead.query(table, columns, selection, selectionArgs, groupBy, having, orderBy,
                limit);
    }

    public void closeCursor(Cursor cursor) {

        if (cursor != null) {
            cursor.close();
        }
    }

    public long insert(String table, ContentValues values) {

        if (dbWrite == null) {
            openWritable();
        }
        Log.v(TAG, "inserting values: " + values + ", to : " + table);
        return dbWrite.insertOrThrow(table, null, values);
    }

    public int delete(String table, String whereClause, String[] whereArgs) {

        if (dbWrite == null) {
            openWritable();
        }
        Log.v(TAG, "deleting from table:" + table + ", whereClause=" + whereClause);
        return dbWrite.delete(table, whereClause, whereArgs);
    }

    public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {

        if (dbWrite == null) {
            openWritable();
        }
        Log.v(TAG, "updating table:" + table + ", values: " + values + ", whereClause="
                + whereClause);
        int count = dbWrite.update(table, values, whereClause, whereArgs);
        Log.v(TAG, "affected entries : " + count);
        return count;
    }

    public void execSQL(String sql) {

        if (dbWrite == null) {
            openWritable();
        }
        Log.v(TAG, "sql:" + sql);
        dbWrite.execSQL(sql);
    }

    protected static void addUniqueFields(StringBuilder sb, String[] fields) {

        if (fields != null) {
            sb.append(" UNIQUE(");
            boolean start = true;
            for (String field : fields) {
                if (!start) {
                    sb.append(",");
                }
                sb.append(field);
                start = false;
            }
            sb.append(") ");
        }

    }

    protected void addSelectQuery(StringBuilder sb, String table, String... fields) {

        addSelectQuery(sb, table, fields, null);
    }

    protected void addSelectQuery(StringBuilder sb, String table, String[] fields,
                                  String customFieldStatement) {

        sb.append("SELECT ");
        if (fields == null || fields.length == 0) {
            sb.append("*");
        } else {
            boolean start = true;
            for (String f : fields) {
                if (!start) {
                    sb.append(",");
                }
                sb.append(f);
                start = false;
            }
            if (!TextUtils.isEmpty(customFieldStatement)) {
                sb.append(",").append(customFieldStatement);
            }
        }
        sb.append(" FROM ").append(table).append(" ");
    }

    protected void addLimitFilter(StringBuilder sb, String orderBy, String sortOrder, int start,
                                  int size) {

        if (!TextUtils.isEmpty(orderBy)) {
            if (orderBy.equals(ConstantGlobal.NAME)) {
                orderBy = "LOWER(" + orderBy + ")";
            }
            sb.append("ORDER BY ").append(orderBy).append(" ");
        }
        if (!TextUtils.isEmpty(sortOrder)) {
            sb.append(sortOrder).append(" ");
        }

        if (size != SQLDBUtil.NO_LIMIT) {
            sb.append("LIMIT ").append(start).append(",").append(size).append(" ");
        }
    }

    protected static void addCreateTableQuery(StringBuilder sb, String tableName) {

        sb.append("create table if not exists ").append(tableName).append(" (");
    }

    protected static void addUpdateQuery(StringBuilder sb, String tableName) {

        sb.append("update table ").append(tableName).append(" ");
    }

    protected static void addUpdateTableQuery(StringBuilder sb, String tableName) {

        sb.append("alter table ").append(tableName).append(" ADD ");
    }

    protected static void endCreateTableQuery(StringBuilder sb) {

        sb.append(");");
    }

    protected boolean addIntEqualSQLQuery(String fieldName, int value, StringBuilder sb) {

        sb.append(fieldName).append("=").append(value).append(SPACE);
        return true;
    }

    protected boolean addIntNotEqualSQLQuery(String fieldName, int value, StringBuilder sb) {

        sb.append(fieldName).append("!=").append(value).append(SPACE);
        return true;
    }

    protected boolean addBooleanEqualSQLQuery(String fieldName, boolean value, StringBuilder sb) {

        sb.append(fieldName).append("=").append(value ? 1 : 0).append(SPACE);
        return true;
    }

    @SuppressLint("DefaultLocale")
    protected boolean addStringEqualSQLQuery(String fieldName, String value, StringBuilder sb,
                                             boolean ignoreCase) {

        if (value == null) {
            return false;
        }
        if (ignoreCase) {
            sb.append("LOWER(").append(fieldName).append(")").append("='")
                    .append(value.toLowerCase()).append("' ");
        } else {
            sb.append(fieldName).append("='").append(value.trim()).append("' ");
        }
        return true;
    }

    protected static void addAbstractAbstractDataModelFeildsRow(StringBuilder sb) {

        sb.append(ConstantGlobal._ID).append(" integer primary key autoincrement,");
        sb.append(ConstantGlobal.ORG_KEY_ID).append(" integer,");
        sb.append(ConstantGlobal.TIME_CREATED).append(" text not null,");
    }

    protected static String createIndexQuery(String tableName, String indexName, boolean unique,
                                             String... fields) {

        StringBuilder sb = new StringBuilder();
        sb.append("create ");
        if (unique) {
            sb.append("unique ");
        }
        sb.append("index if not exists " + indexName);
        sb.append(" on " + tableName);
        sb.append("(" + TextUtils.join(",", fields) + ")");
        return sb.toString();
    }

    public void triggerSync() {

        context.sendBroadcast(new Intent(context, NetworkMonitor.class));
    }

    public abstract void createDummyData();

    public abstract void cleanData();

    // public abstract void createTable(Set<String> createTables);
    public void assignmentTriggerSync() {

        context.sendBroadcast(new Intent(context, AssignmentNetworkMonitor.class));
    }

    public List<AssignmentAnalytics> getAllAssignmentAnalytics(int orgKeyId, String userId,
                                                               Collection<String> entityIds, String entityType, String[] fields, boolean endedOnly) {

        List<AssignmentAnalytics> AssignmentAnalytics = new ArrayList<AssignmentAnalytics>();
        if (TextUtils.isEmpty(userId)) {
            return AssignmentAnalytics;
        }

        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, TABLE_ENTITY_ANALYTICS, fields, null);
        sb.append("WHERE ");
        if (endedOnly) {
            sb.append(ConstantGlobal.END_TIME).append("!='" + 0 + "' ");
            sb.append(" AND ");

        }

        if (addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false)) {
            sb.append("AND ");
        }

        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_TYPE, entityType, sb, false)) {
            sb.append("AND ");
        }

        if (entityIds != null && !entityIds.isEmpty()) {
            sb.append(ConstantGlobal.ENTITY_ID).append(" in (")
                    .append(LocalManager.joinString(entityIds, "'")).append(") ");
            sb.append("AND ");
        }
        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);

        addLimitFilter(sb, ConstantGlobal.TIME_CREATED, SQLDBUtil.ORDER_DESC, SQLDBUtil.NO_START,
                SQLDBUtil.NO_LIMIT);
        Cursor cursor = rawQuery(sb.toString(), null);
        if (cursor != null && cursor.moveToFirst()) {
            Set<String> cColumnNames = new HashSet<String>(Arrays.asList(cursor.getColumnNames()));
            while (!cursor.isAfterLast()) {
                AssignmentAnalytics tA = SQLDBUtil.convertToValues(cursor, AssignmentAnalytics.class, fields,
                        cColumnNames);
                AssignmentAnalytics.add(tA);
                cursor.moveToNext();
            }
            cColumnNames.clear();
            cColumnNames = null;
        }
        closeCursor(cursor);
        return AssignmentAnalytics;
    }
}
