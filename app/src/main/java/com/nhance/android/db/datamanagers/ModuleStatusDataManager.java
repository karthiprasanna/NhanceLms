package com.nhance.android.db.datamanagers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.nhance.android.async.ISyncableContainer;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.models.UserModuleEntryStatus;
import com.nhance.android.enums.EntityType;
import com.nhance.android.pojos.SrcEntity;
import com.nhance.android.pojos.content.infos.ModuleExtendedInfo;
import com.nhance.android.utils.SQLDBUtil;

public class ModuleStatusDataManager extends AbstractDataManager implements ISyncableContainer {

    private static final String TAG                            = "ModuleStatusDataManager";
    public static final String  TABLE_USER_MODULE_ENTRY_STATUS = "user_module_entry_status";

    public ModuleStatusDataManager(Context context) {

        //
        super(context);
    }

    public void updateModuleEntryStatus(String userId, int orgKeyId, String moduleId,
            String entityId, String entityType, boolean synced) throws Exception {

        Log.e("inside update module", ".......");
        if (TextUtils.isEmpty(userId)) {
            return;
        }
        UserModuleEntryStatus userModuleEntryStatus = getModuleEntryStatus(userId, orgKeyId,
                moduleId, entityId, entityType);

        if (userModuleEntryStatus != null && !userModuleEntryStatus.synced && synced) {
            userModuleEntryStatus.synced = synced;
            ContentValues values = userModuleEntryStatus.toContentValues();
            Log.e("inside update module 1", ".......");
            update(TABLE_USER_MODULE_ENTRY_STATUS, values, "_id=" + userModuleEntryStatus._id, null);
            Log.d(TAG, "user has already seen this content");
        } else if (userModuleEntryStatus == null) {
            Log.e("inside update module2", ".......");
            userModuleEntryStatus = new UserModuleEntryStatus(orgKeyId, userId, moduleId, entityId,
                    entityType);
            userModuleEntryStatus.synced = synced;
            Log.d(TAG, "inserting into " + TABLE_USER_MODULE_ENTRY_STATUS + " table");
            ContentValues values = userModuleEntryStatus.toContentValues();
            insert(TABLE_USER_MODULE_ENTRY_STATUS, values);
            if (!synced) {
                sync();
            }
        }
    }

    public void updateModuleEntryStatus(String userId, int orgKeyId, String moduleId,
            String entityId, String entityType) throws Exception {

        updateModuleEntryStatus(userId, orgKeyId, moduleId, entityId, entityType, false);
    }

    public void syncModuleEntryStatus(String userId, int orgKeyId, String moduleId,
            List<SrcEntity> entities, boolean synced) throws Exception {

        if (TextUtils.isEmpty(userId)) {
            return;
        }
        for (SrcEntity entity : entities) {
            updateModuleEntryStatus(userId, orgKeyId, moduleId, entity.id, entity.type.toString(),
                    synced);
        }
    }

    public UserModuleEntryStatus getModuleEntryStatus(String userId, int orgKeyId, String moduleId,
            String entityId, String entityType) {

        UserModuleEntryStatus userModuleStatus = null;
        if (TextUtils.isEmpty(userId)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, TABLE_USER_MODULE_ENTRY_STATUS);
        sb.append(" WHERE ");
        if (addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false)) {
            sb.append(" AND ");
        }
        if (addStringEqualSQLQuery(ConstantGlobal.MODULE_ID, moduleId, sb, false)) {
            sb.append(" AND ");
        }
        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_ID, entityId, sb, false)) {
            sb.append(" AND ");
        }
        if (addStringEqualSQLQuery(ConstantGlobal.ENTITY_TYPE, entityType, sb, false)) {
            sb.append(" AND ");
        }
        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);

        Cursor cursor = rawQuery(sb.toString(), null);
        if (cursor != null && cursor.moveToFirst()) {
            userModuleStatus = SQLDBUtil.convertToValues(cursor, UserModuleEntryStatus.class, null);
        }
        closeCursor(cursor);

        return userModuleStatus;
    }

    /**
     * 
     * @param userId
     * @param orgKeyId
     * @param moduleId
     * @return will only return user consumed module entry list
     */
    public Map<SrcEntity, UserModuleEntryStatus> getModuleEntryStatus(String userId, int orgKeyId,
            String moduleId) {

        Map<SrcEntity, UserModuleEntryStatus> userModuleEntryStatus = new LinkedHashMap<SrcEntity, UserModuleEntryStatus>();
        if (TextUtils.isEmpty(userId)) {
            return userModuleEntryStatus;
        }
        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, TABLE_USER_MODULE_ENTRY_STATUS);
        sb.append(" WHERE ");
        if (addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false)) {
            sb.append(" AND ");
        }
        if (addStringEqualSQLQuery(ConstantGlobal.MODULE_ID, moduleId, sb, false)) {
            sb.append(" AND ");
        }
        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);

        Cursor cursor = rawQuery(sb.toString(), null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                UserModuleEntryStatus userModuleStatus = SQLDBUtil.convertToValues(cursor,
                        UserModuleEntryStatus.class, null);
                userModuleEntryStatus.put(
                        new SrcEntity(EntityType.valueOfKey(userModuleStatus.entityType),
                                userModuleStatus.entityId), userModuleStatus);
            }
        }
        closeCursor(cursor);

        return userModuleEntryStatus;
    }

    public List<UserModuleEntryStatus> getModuleEntriesStatus(String userId, int orgKeyId,
            String moduleId) {

        List<UserModuleEntryStatus> userModuleEntriesStatus = new ArrayList<UserModuleEntryStatus>();
        if (TextUtils.isEmpty(userId)) {
            return userModuleEntriesStatus;
        }
        StringBuilder sb = new StringBuilder();
        addSelectQuery(sb, TABLE_USER_MODULE_ENTRY_STATUS);
        sb.append(" WHERE ");
        if (addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false)) {
            sb.append(" AND ");
        }
        if (addStringEqualSQLQuery(ConstantGlobal.MODULE_ID, moduleId, sb, false)) {
            sb.append(" AND ");
        }
        addIntEqualSQLQuery(ConstantGlobal.SYNCED, 0, sb);
        sb.append(" AND ");
        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);

        Cursor cursor = rawQuery(sb.toString(), null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                UserModuleEntryStatus userModuleStatus = SQLDBUtil.convertToValues(cursor,
                        UserModuleEntryStatus.class, null);
                userModuleEntriesStatus.add(userModuleStatus);
            }
        }
        closeCursor(cursor);
        return userModuleEntriesStatus;
    }

    public void annotateModuleStatusInfo(List<ModuleExtendedInfo> modules,
            boolean addChildrenDetailAndStatus) {

    }

    @Override
    public void createDummyData() {

    }

    @Override
    public void cleanData() {

        try {
            execSQL("DROP TABLE " + TABLE_USER_MODULE_ENTRY_STATUS);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public static void createTable(List<String> createTables) {

        createTables.add(createUserModuleStatusTable());
        createTables
                .add(createIndexQuery(TABLE_USER_MODULE_ENTRY_STATUS, "user_module_status_index",
                        false, ConstantGlobal.MODULE_ID, ConstantGlobal.USER_ID));
    }

    private static String createUserModuleStatusTable() {

        StringBuilder sb = new StringBuilder();
        addCreateTableQuery(sb, TABLE_USER_MODULE_ENTRY_STATUS);
        addAbstractAbstractDataModelFeildsRow(sb);
        sb.append(ConstantGlobal.USER_ID + " text not null,");
        sb.append(ConstantGlobal.MODULE_ID + " text not null,");
        sb.append(ConstantGlobal.ENTITY_ID + " text not null,");
        sb.append(ConstantGlobal.ENTITY_TYPE + " text not null,");
        sb.append(ConstantGlobal.SYNCED + " integer");
        endCreateTableQuery(sb);
        return sb.toString();
    }


    @Override
    public void sync() {

        super.triggerSync();
    }

}
