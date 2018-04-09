package com.nhance.android.db.datamanagers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.models.SDCardFileMetadata;
import com.nhance.android.enums.EntityType;
import com.nhance.android.utils.SQLDBUtil;

public class SDCardFileMetadataManager extends AbstractDataManager {

    private static final String TABLE = "sdcard_file_data";
    private static final String TAG   = "SDCardFileMetadataManager";

    public SDCardFileMetadataManager(Context context) {

        super(context);
    }

    public int insert(SDCardFileMetadata fileMetadata) throws Exception {

        if (TextUtils.isEmpty(fileMetadata.userId)) {
            return -1;
        }
        ContentValues values = fileMetadata.toContentValues();
        fileMetadata._id = (int) insert(TABLE, values);
        return fileMetadata._id;
    }

    public SDCardFileMetadata getSDCardFileMetadata(int orgKeyId, String userId, String targetId,
            String targetType, String entityId, String entityType) {

        return getSDCardFileMetadata(orgKeyId, userId, targetId, targetType, entityId, entityType,
                null, null);
    }

    public SDCardFileMetadata
            getSDCardFileMetadata(int orgKeyId, String userId, String targetId, String targetType,
                    String entityId, String entityType, String name, Boolean activeSDCard) {

        SDCardFileMetadata cardFileMetadata = null;
        if (TextUtils.isEmpty(userId)) {
            return cardFileMetadata;
        }
        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, TABLE);
        sb.append(SQL_KEYWORD_WHERE);
        if (addStringEqualSQLQuery(ConstantGlobal.TARGET_ID, targetId, sb, false)) {
            sb.append(SQL_KEYWORD_AND);
        }

        if (addStringEqualSQLQuery(ConstantGlobal.TARGET_TYPE, targetType, sb, false)) {
            sb.append(SQL_KEYWORD_AND);
        }

        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_ID, entityId, sb, false)) {
            sb.append(SQL_KEYWORD_AND);
        }

        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_TYPE, entityType, sb, false)) {
            sb.append(SQL_KEYWORD_AND);
        }

        if (addStringEqualSQLQuery(ConstantGlobal.NAME, name, sb, false)) {
            sb.append(SQL_KEYWORD_AND);
        }

        if (activeSDCard != null) {
            addBooleanEqualSQLQuery(SDCardFileMetadata.FIELD_ACTIVE, activeSDCard, sb);
            sb.append(SQL_KEYWORD_AND);
        }

        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);

        Cursor cursor = rawQuery(sb.toString(), null);
        if (cursor.moveToFirst()) {
            cardFileMetadata = SQLDBUtil.convertToValues(cursor, SDCardFileMetadata.class, null);
        }
        closeCursor(cursor);
        Log.d(TAG, "returning SDCardFileMetadata : " + cardFileMetadata);
        return cardFileMetadata;
    }

    public Map<EntityType, Set<String>> getActiveSDCardContent(int orgKeyId, String userId,
            String targetId, String targetType) {

        Map<EntityType, Set<String>> sdcardContentMap = new HashMap<EntityType, Set<String>>();
        if (TextUtils.isEmpty(userId)) {
            return sdcardContentMap;
        }

        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, TABLE);
        sb.append(SQL_KEYWORD_WHERE);
        if (addStringEqualSQLQuery(ConstantGlobal.TARGET_ID, targetId, sb, false)) {
            sb.append(SQL_KEYWORD_AND);
        }

        if (addStringEqualSQLQuery(ConstantGlobal.TARGET_TYPE, targetType, sb, false)) {
            sb.append(SQL_KEYWORD_AND);
        }

        if (addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false)) {
            sb.append(SQL_KEYWORD_AND);
        }

        addBooleanEqualSQLQuery(SDCardFileMetadata.FIELD_ACTIVE, true, sb);
        sb.append(SQL_KEYWORD_AND);
        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);

        Cursor cursor = rawQuery(sb.toString(), null);
        while (cursor.moveToNext()) {
            SDCardFileMetadata mData = SQLDBUtil.convertToValues(cursor, SDCardFileMetadata.class,
                    null);
            EntityType eType = EntityType.valueOfKey(mData.entityType);
            if (sdcardContentMap.get(eType) == null) {
                sdcardContentMap.put(eType, new HashSet<String>());
            }
            sdcardContentMap.get(eType).add(mData.entityId);
        }
        closeCursor(cursor);
        return sdcardContentMap;
    }

    public int updateActiveSDCard(String cardId, boolean active, String mountPath) {

        ContentValues values = new ContentValues();
        values.put(SDCardFileMetadata.FIELD_ACTIVE, active ? 1 : 0);
        values.put(SDCardFileMetadata.FIELD_MOUNT_PATH,
                mountPath == null ? org.apache.commons.lang.StringUtils.EMPTY : mountPath);
        Log.d(TAG, "updates values: " + values);
        return update(TABLE, values, TextUtils.isEmpty(cardId) ? null
                : (SDCardFileMetadata.FIELD_CARD_ID + "='" + cardId + "'"), null);
    }

    public static void createTable(List<String> createTables) {

        StringBuilder sb = new StringBuilder();
        addCreateTableQuery(sb, TABLE);
        addAbstractAbstractDataModelFeildsRow(sb);
        sb.append(ConstantGlobal.USER_ID + " text not null,");
        sb.append(ConstantGlobal.NAME + " text not null,");
        sb.append(ConstantGlobal.TARGET_ID + " text not null,");
        sb.append(ConstantGlobal.TARGET_TYPE + " text not null,");
        sb.append(ConstantGlobal.ENTITY_ID + " text not null,");
        sb.append(ConstantGlobal.ENTITY_TYPE + " text not null,");
        sb.append(SDCardFileMetadata.FIELD_LOCATION + " text not null,");
        sb.append(SDCardFileMetadata.FIELD_SIZE + " text not null,");
        sb.append(SDCardFileMetadata.FIELD_CARD_ID + " text not null,");
        sb.append(SDCardFileMetadata.FIELD_CARD_NAME + " text not null,");
        sb.append(SDCardFileMetadata.FIELD_MOUNT_PATH + " text,");
        sb.append(SDCardFileMetadata.FIELD_ACTIVE + " integer");
        endCreateTableQuery(sb);
        createTables.add(sb.toString());

        createTables.add(createIndexQuery(TABLE, "sdcard_entity_index", true,
                ConstantGlobal.ENTITY_ID, ConstantGlobal.ENTITY_TYPE,
                SDCardFileMetadata.FIELD_CARD_ID, ConstantGlobal.NAME));
        createTables.add(createIndexQuery(TABLE, "sdcard_file_location_index", false,
                SDCardFileMetadata.FIELD_CARD_ID));

    }

    @Override
    public void createDummyData() {

    }

    @Override
    public void cleanData() {

    }

}
