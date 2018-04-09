package com.nhance.android.db.datamanagers;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.models.UserOrgProfile;
import com.nhance.android.db.models.entity.User;
import com.nhance.android.utils.SQLDBUtil;

public class UserDataManager extends AbstractDataManager {

    private static final String TAG            = "UserDataManager";
    private static final String TABLE          = "user";
    private static final String TABLE_ORG_USER = "orguser";

    public UserDataManager(Context context) {

        super(context);
    }

    public void insertUser(User user) throws Exception {

        Log.d(TAG, " Inserting user in to DB");
        ContentValues values = user.toContentValues();
        user._id = (int) insert(TABLE, values);
    }

    public void insertUserOrgProfile(UserOrgProfile userOrgProfile) throws Exception {

        Log.d(TAG, " Inserting userOrgProfile in to DB");
        ContentValues values = userOrgProfile.toContentValues();
        userOrgProfile._id = (int) insert(TABLE_ORG_USER, values);
    }

    public User getUser(int orgKeyId, String userId) {

        User user = null;
        if (TextUtils.isEmpty(userId)) {
            return user;
        }

        StringBuilder sb = new StringBuilder();

        addSelectQuery(sb, TABLE);
        sb.append("WHERE ");
        if (addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false)) {
            sb.append("AND ");
        }
        addIntEqualSQLQuery(ConstantGlobal.ORG_KEY_ID, orgKeyId, sb);

        Cursor cursor = rawQuery(sb.toString(), null);

        if (cursor != null && cursor.moveToFirst()) {
            user = SQLDBUtil.convertToValues(cursor, User.class, null);
        }
        closeCursor(cursor);
        Log.d(TAG, "returning user:" + user);
        return user;
    }

    public User getUser(String username, String password) {

        User user = null;
        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            return user;
        }
        StringBuilder sb = new StringBuilder();

        addSelectQuery(sb, TABLE);
        sb.append("WHERE ");
        addStringEqualSQLQuery(ConstantGlobal.USERNAME, username, sb, false);
        sb.append(SQL_KEYWORD_AND).append(SPACE);
        addStringEqualSQLQuery(ConstantGlobal.PASSWORD, password, sb, false);

        Cursor cursor = rawQuery(sb.toString(), null);

        if (cursor != null && cursor.moveToFirst()) {
            user = SQLDBUtil.convertToValues(cursor, User.class, null);
        }
        closeCursor(cursor);
        Log.d(TAG, "returning user:" + user);
        return user;
    }

    public UserOrgProfile getUserOrgProfile(String userId, String orgId) {

        UserOrgProfile userOrgProfile = null;
        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(orgId)) {
            return userOrgProfile;
        }
        StringBuilder sb = new StringBuilder();

        addSelectQuery(sb, TABLE_ORG_USER);
        sb.append("WHERE ");
        addStringEqualSQLQuery(ConstantGlobal.USER_ID, userId, sb, false);
        sb.append("AND ");
        addStringEqualSQLQuery(ConstantGlobal.ORG_ID, orgId, sb, false);
        Cursor cursor = rawQuery(sb.toString(), null);

        if (cursor != null && cursor.moveToFirst()) {
            userOrgProfile = SQLDBUtil.convertToValues(cursor, UserOrgProfile.class, null);
        }
        closeCursor(cursor);
        Log.d(TAG, "returning user:" + userOrgProfile);
        return userOrgProfile;
    }

    public int updateUser(User user) {

        ContentValues values = null;
        try {
            values = user.toContentValues();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        if (values == null) {
            return -1;
        }
        return update(TABLE, values, "_id=" + user._id, null);
    }

    public int updateUserOrgProfile(UserOrgProfile userOrgProfile) {

        ContentValues values = null;
        try {
            values = userOrgProfile.toContentValues();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        if (values == null) {
            return -1;
        }
        return update(TABLE_ORG_USER, values, "_id=" + userOrgProfile._id, null);
    }

    @Override
    public void createDummyData() {

    }

    @Override
    public void cleanData() {

        Log.d(TAG, "cleaning user data");
        try {
            execSQL("DROP TABLE " + TABLE);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        try {
            execSQL("DROP TABLE " + TABLE_ORG_USER);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    public static void createTable(List<String> createTables) {

        Log.d(TAG, "creating user and user orgProfile table");
        createTables.add(createUserTable());
        createTables.add(createOrgUserTable());

        createTables.add(createIndexQuery(TABLE, "user_id", true, ConstantGlobal.USER_ID));
        createTables.add(createIndexQuery(TABLE, "user_login", true, ConstantGlobal.USERNAME,
                ConstantGlobal.PASSWORD));
        createTables.add(createIndexQuery(TABLE_ORG_USER, "user_org_prof", true,
                ConstantGlobal.USER_ID, ConstantGlobal.ORG_ID));
    }

    private static String createUserTable() {

        StringBuilder sb = new StringBuilder();

        addCreateTableQuery(sb, TABLE);

        addAbstractAbstractDataModelFeildsRow(sb);
        sb.append(ConstantGlobal.USER_ID).append(" text not null,");
        sb.append(ConstantGlobal.USERNAME).append(" text not null,");
        sb.append(ConstantGlobal.FIRST_NAME).append(" text not null,");
        sb.append(ConstantGlobal.LAST_NAME).append(" text,");
        sb.append(ConstantGlobal.THUMB).append(" text not null,");
        sb.append(ConstantGlobal.PASSWORD).append(" text not null,");
        sb.append(ConstantGlobal.LAST_LOGIN).append(" text not null,");
        sb.append("autoLogin").append(" integer,");
        sb.append(ConstantGlobal.KEY).append(" blob");

        endCreateTableQuery(sb);

        return sb.toString();

    }

    private static String createOrgUserTable() {

        StringBuilder sb = new StringBuilder();

        addCreateTableQuery(sb, TABLE_ORG_USER);

        addAbstractAbstractDataModelFeildsRow(sb);
        sb.append(ConstantGlobal.USER_ID).append(" text not null,");
        sb.append(ConstantGlobal.ORG_ID).append(" text not null,");
        sb.append(ConstantGlobal.ORG_PROFILE).append(" text not null");
        endCreateTableQuery(sb);
        return sb.toString();
    }

}
