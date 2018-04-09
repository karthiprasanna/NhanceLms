package com.nhance.android.db.datamanagers;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.models.SDCardGroup;
import com.nhance.android.pojos.content.sdcards.SDCardGroupInfo;
import com.nhance.android.utils.SQLDBUtil;

public class SDCardGroupDataManager extends AbstractDataManager {

    private static final String TABLE = "sd_card_group_info";
    private static final String TAG   = "SDCardGroupDataManager";

    public SDCardGroupDataManager(Context context) {

        super(context);
    }

    public SDCardGroup upsertGroupInfo(SDCardGroup groupInfo) throws Exception {

        Log.v(TAG, "inserting into " + TABLE + " table");
        SDCardGroup presentSDCardGroup = getSDCardGroup(groupInfo.id, groupInfo.userId,
                groupInfo.orgKeyId);
        ContentValues values = groupInfo.toContentValues();
        if (presentSDCardGroup != null) {
            groupInfo._id = update(TABLE, values, "_id=" + presentSDCardGroup._id, null);
        } else {
            groupInfo._id = (int) insert(TABLE, values);
        }
        return groupInfo;
    }

    public SDCardGroup getSDCardGroup(String id, String userId, int orgKeyId) {

        SDCardGroup group = null;
        StringBuilder sb = new StringBuilder();

        addSelectQuery(sb, TABLE);
        sb.append("WHERE ");
        addStringEqualSQLQuery(ConstantGlobal.ID, id, sb, false);
        sb.append(" AND ");
        addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false);
        sb.append(" AND ");
        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);

        Cursor cursor = rawQuery(sb.toString(), null);

        if (cursor != null && cursor.moveToFirst()) {
            group = SQLDBUtil.convertToValues(cursor, SDCardGroup.class, null);
        }
        closeCursor(cursor);
        Log.v(TAG, "returning sdCardGroup:" + group);

        return group;
    }

    @Override
    public void createDummyData() {

    }

    @Override
    public void cleanData() {

    }

    public static void createTable(List<String> createTables) {

        StringBuilder sb = new StringBuilder();
        addCreateTableQuery(sb, TABLE);
        addAbstractAbstractDataModelFeildsRow(sb);
        sb.append(ConstantGlobal.NAME + " text not null,");
        sb.append(ConstantGlobal.ID + " text not null,");
        sb.append(ConstantGlobal.USER_ID + " text not null,");
        sb.append(ConstantGlobal.TARGET_ID + " text not null,");
        sb.append(ConstantGlobal.TARGET_TYPE + " text not null,");
        sb.append(SDCardGroupInfo.FIELD_SIZE + " text not null,");
        sb.append(SDCardGroup.FIELD_ACTIVATED + " integer,");
        sb.append(SDCardGroup.FIELD_ACCESS_CODE + " text,");
        sb.append(SDCardGroup.FIELD_ACTIVATED_TIME + " text");
        endCreateTableQuery(sb);
        createTables.add(sb.toString());

        createTables.add(createIndexQuery(TABLE, "sdcarg_group_id", true, ConstantGlobal.ID,
                ConstantGlobal.USER_ID));

    }

}
