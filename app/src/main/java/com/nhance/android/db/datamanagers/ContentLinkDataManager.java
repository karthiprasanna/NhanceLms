package com.nhance.android.db.datamanagers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.models.entity.ContentLink;
import com.nhance.android.utils.SQLDBUtil;

public class ContentLinkDataManager extends AbstractDataManager {

    private static final String TAG   = "ContentLinkManager";
    public static final String  TABLE = "contentlink";

    public ContentLinkDataManager(Context context) {

        super(context);
    }

    public ContentLink getContentLink(String entityId, String entityType, String userId,
            String targetId, String targetType, int orgKeyId) {

        ContentLink contentLink = null;
        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(entityId)) {
            return contentLink;
        }
        StringBuilder sb = new StringBuilder();

        addSelectQuery(sb, TABLE);
        sb.append("WHERE ");
        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_ID, entityId, sb, false)) {
            sb.append(SQL_KEYWORD_AND);
        }

        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_TYPE, entityType, sb, false)) {
            sb.append(SQL_KEYWORD_AND);
        }

        if (addStringEqualSQLQuery(ConstantGlobal.TARGET_ID, targetId, sb, false)) {
            sb.append(SQL_KEYWORD_AND);
        }

        if (addStringEqualSQLQuery(ConstantGlobal.TARGET_TYPE, targetType, sb, false)) {
            sb.append(SQL_KEYWORD_AND);
        }
        addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false);
        sb.append(SQL_KEYWORD_AND);
        sb.append(ConstantGlobal.ORG_KEY_ID).append("=").append(orgKeyId);

        Cursor cursor = rawQuery(sb.toString(), null);

        if (cursor != null && cursor.moveToFirst()) {
            contentLink = SQLDBUtil.convertToValues(cursor, ContentLink.class, null);
        }
        closeCursor(cursor);
        Log.d(TAG, "returning content:" + contentLink);
        return contentLink;
    }

    public void insertLink(ContentLink link) throws Exception {

        Log.d(TAG, "inserting into contentlink table");
        ContentValues values = link.toContentValues();
        insert(TABLE, values);
    }

    public int updateLink(ContentLink link) {

        ContentValues values = null;
        try {
            values = link.toContentValues();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        if (values == null) {
            return -1;
        }
        return update(TABLE, values, "_id=" + link._id, null);
    }

    public int updateContentLink(String linkId, ContentValues tobeUpdatedValues) {

        if (tobeUpdatedValues == null) {
            return -1;
        }
        return update(TABLE, tobeUpdatedValues, "linkId='" + linkId + "'", null);
    }

    public ContentLink getContentLink(String linkId) {

        Log.d(TAG, "in getContentLink");
        ContentLink contentLink = null;
        StringBuilder sb = new StringBuilder();

        addSelectQuery(sb, TABLE);
        sb.append("WHERE ");

        addStringEqualSQLQuery(ConstantGlobal.LINK_ID, linkId, sb, false);

        Cursor cursor = rawQuery(sb.toString(), null);

        if (cursor != null && cursor.moveToFirst()) {
            contentLink = SQLDBUtil.convertToValues(cursor, ContentLink.class, null);
        }
        closeCursor(cursor);
        Log.d(TAG, "returning ContentLink:" + contentLink);
        return contentLink;

    }

    public List<ContentLink> getLibraryContentLinks(int orgKeyId, String userId, String targetId,
            String targetType) {

        List<ContentLink> contentLinks = new ArrayList<ContentLink>();
        if (TextUtils.isEmpty(userId)) {
            return contentLinks;
        }

        StringBuilder sb = new StringBuilder();

        addSelectQuery(sb, TABLE);
        sb.append("WHERE ");

        if (addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb)) {
            sb.append("AND ");
        }

        if (addStringEqualSQLQuery(ConstantGlobal.TARGET_ID, targetId, sb, false)) {
            sb.append("AND ");
        }

        if (addStringEqualSQLQuery(ConstantGlobal.TARGET_TYPE, targetType, sb, false)) {
            sb.append("AND ");
        }

        addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false);

        addLimitFilter(sb, ConstantGlobal.POSITION, SQLDBUtil.ORDER_ASC, SQLDBUtil.NO_START,
                SQLDBUtil.NO_LIMIT);

        Cursor cursor = rawQuery(sb.toString(), null);

        if (cursor != null && cursor.moveToFirst()) {
            Set<String> cColumnNames = new HashSet<String>(Arrays.asList(cursor.getColumnNames()));
            while (!cursor.isAfterLast()) {
                ContentLink contentLink = SQLDBUtil.convertToValues(cursor, ContentLink.class,
                        null, cColumnNames);
                contentLinks.add(contentLink);
                cursor.moveToNext();
            }
            cColumnNames.clear();
            cColumnNames = null;
        }
        closeCursor(cursor);
        Log.d(TAG, "returning contentLinks:" + contentLinks);

        return contentLinks;
    }

    /**
     * 
     * @param id
     * @param type
     * @return only { "id", "type", "downloadable" } fields
     */

    public List<ContentLink> getContentLinks(int orgKeyId, String userId, String entityId,
            String entityType, Boolean downloadable) {

        List<ContentLink> contentLinks = new ArrayList<ContentLink>();
        if (TextUtils.isEmpty(userId)) {
            return contentLinks;
        }
        StringBuilder sb = new StringBuilder();
        String[] fields = new String[] { ConstantGlobal.TARGET_ID, ConstantGlobal.TARGET_TYPE,
                ConstantGlobal.DOWNLOADABLE };
        addSelectQuery(sb, TABLE, fields);
        sb.append("WHERE ");

        if (addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb)) {
            sb.append("AND ");
        }

        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_ID, entityId, sb, false)) {
            sb.append("AND ");
        }

        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_TYPE, entityType, sb, false)) {
            sb.append("AND ");
        }

        if (downloadable != null) {
            addBooleanEqualSQLQuery(ConstantGlobal.DOWNLOADABLE, downloadable, sb);
            sb.append("AND ");
        }

        addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false);

        addLimitFilter(sb, ConstantGlobal.POSITION, SQLDBUtil.ORDER_ASC, SQLDBUtil.NO_START,
                SQLDBUtil.NO_LIMIT);

        Cursor cursor = rawQuery(sb.toString(), null);

        if (cursor != null && cursor.moveToFirst()) {
            Set<String> cColumnNames = new HashSet<String>(Arrays.asList(cursor.getColumnNames()));
            while (!cursor.isAfterLast()) {
                ContentLink contentLink = SQLDBUtil.convertToValues(cursor, ContentLink.class,
                        fields, cColumnNames);
                contentLinks.add(contentLink);
                cursor.moveToNext();
            }
            cColumnNames.clear();
            cColumnNames = null;
        }
        closeCursor(cursor);
        Log.d(TAG, "returning contentLinks:" + contentLinks);
        return contentLinks;
    }

    public int deleteLink(int id) {

        return delete(TABLE, "_id=" + id, null);
    }

    @Override
    public void createDummyData() {

    }

    @Override
    public void cleanData() {

        try {
            execSQL("DROP TABLE IF EXISTS " + TABLE);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public static void updateTable(List<String> updateTables) {

        Log.d(TAG, "............in update table" + "..............");
        StringBuilder sb = new StringBuilder();
        addUpdateTableQuery(sb, TABLE);
        sb.append(ConstantGlobal.DOWNLOADABLE_ENTITIES + " text");
        updateTables.add(sb.toString());

        // update position
        StringBuilder upsb = new StringBuilder();
        addUpdateTableQuery(upsb, TABLE);
        upsb.append(ConstantGlobal.POSITION + " integer");
        updateTables.add(upsb.toString());

    }

    public static void createTable(List<String> createTables) {

        Log.d(TAG, "............in create table" + "..............");

        updateTable(createTables);

        StringBuilder sb = new StringBuilder();
        addCreateTableQuery(sb, TABLE);
        addAbstractAbstractDataModelFeildsRow(sb);
        sb.append(ConstantGlobal.USER_ID + " text not null,");
        sb.append(ConstantGlobal.LINK_ID + " text not null,");
        sb.append(ConstantGlobal.ENTITY_ID + " text not null,");
        sb.append(ConstantGlobal.ENTITY_TYPE + " text not null,");
        sb.append(ConstantGlobal.TARGET_ID + " text not null,");
        sb.append(ConstantGlobal.TARGET_TYPE + " text not null,");
        sb.append(ConstantGlobal.LAST_UPDATED + " text,");
        sb.append(ConstantGlobal.PASSPHRASE).append(" text,");
        sb.append(ConstantGlobal.ENC_LEVEL).append(" text,");
        sb.append(ConstantGlobal.DOWNLOADABLE + " integer,");
        sb.append(ConstantGlobal.DOWNLOADABLE_ENTITIES + " text,");
        sb.append(ConstantGlobal.POSITION + " integer");
        endCreateTableQuery(sb);
        createTables.add(sb.toString());
        createTables.add(createIndexQuery(TABLE, "content_link_id", true, ConstantGlobal.LINK_ID,
                ConstantGlobal.ORG_KEY_ID, ConstantGlobal.USER_ID));
        createTables.add(createIndexQuery(TABLE, "content_link", false, ConstantGlobal.TARGET_ID,
                ConstantGlobal.TARGET_TYPE, ConstantGlobal.ENTITY_ID, ConstantGlobal.ENTITY_TYPE,
                ConstantGlobal.USER_ID, ConstantGlobal.ORG_KEY_ID));

    }

}
