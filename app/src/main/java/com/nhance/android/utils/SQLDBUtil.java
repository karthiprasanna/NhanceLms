package com.nhance.android.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.util.Log;

public class SQLDBUtil {

    private static final String TAG             = "SQLDBUtil";
    public static final int     NO_LIMIT        = 0;
    public static final int     NO_START        = 0;
    public static final int     NO_INT_VALUE    = -1;
    public static final int     INSERT          = 0;
    public static final int     UPDATE          = 1;
    public static final String  ORDER_ASC       = "ASC";
    public static final String  ORDER_DESC      = "DESC";
    public static final String  SEPARATOR       = "#";
    public static final String  SEPARATOR_COMMA = ",";

    public static <T> T convertToValues(Cursor cursor, Class<T> clazz, String[] fields) {

        return convertToValues(cursor, clazz, fields, null);
    }

    public static <T> T convertToValues(Cursor cursor, Class<T> clazz, String[] fields,
            Set<String> cColumnNames) {

        T object = null;
        try {
            object = clazz.newInstance();
            if (fields == null || fields.length == 0) {
                for (Field f : clazz.getFields()) {
                    setValue(f, cursor, object, cColumnNames);
                    // f.set(object, getObject(cursor, f.getName()));
                }
            } else {
                for (String field : fields) {
                    Field f = clazz.getField(field);
                    if (f != null) {
                        setValue(f, cursor, object, cColumnNames);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        }
        return object;
    }

    private static <T> void setValue(Field f, Cursor cursor, T object, Set<String> cColumnName)
            throws IllegalArgumentException, IllegalAccessException {

        if (Modifier.isStatic(f.getModifiers())
                || (cColumnName != null && !cColumnName.contains(f.getName()))) {
            return;
        }
        // Log.d(TAG, "fieldName:" + f.getName());
        int cIndex = cursor.getColumnIndex(f.getName());
        // Object value = getObject(cursor, f.getName());

        if (String.class.equals(f.getType())) {
            f.set(object, cursor.getString(cIndex));
        } else if (int.class.equals(f.getType())) {
            f.set(object, cursor.getInt(cIndex));
        } else if (JSONObject.class.equals(f.getType())) {
            String val = cursor.getString(cIndex);
            try {
                f.set(object, val == null ? new JSONArray() : new JSONObject(val));
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        } else if (JSONArray.class.equals(f.getType())) {
            String val = cursor.getString(cIndex);
            try {
                f.set(object, val == null ? new JSONArray() : new JSONArray(val));
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage(), e);
            }
        } else if (boolean.class.equals(f.getType())) {
            int val = cursor.getInt(cIndex);
            f.set(object, val > 0 ? true : false);
        } else if (double.class.equals(f.getType())) {
            f.set(object, cursor.getDouble(cIndex));
        } else if (long.class.equals(f.getType())) {
            f.set(object, cursor.getLong(cIndex));
        } else if (float.class.equals(f.getType())) {
            f.set(object, cursor.getFloat(cIndex));
        } else if (byte[].class.equals(f.getType())) {
            f.set(object, cursor.getBlob(cIndex));
        } else {
            f.set(object, cursor.getString(cIndex));
        }
    }

    // @Deprecated
    // /**
    // * as this api is not supported on api level < 11
    // * @param cursor
    // * @param columnName
    // * @return
    // */
    // public static Object getObject(Cursor cursor, String columnName) {
    //
    // Object value = null;
    // int cIndex = cursor.getColumnIndex(columnName);
    //
    // switch (cursor.getType(cIndex)) {
    // case Cursor.FIELD_TYPE_INTEGER:
    // value = cursor.getInt(cIndex);
    // break;
    // case Cursor.FIELD_TYPE_STRING:
    // value = cursor.getString(cIndex);
    // break;
    // case Cursor.FIELD_TYPE_FLOAT:
    // value = cursor.getFloat(cIndex);
    // break;
    // case Cursor.FIELD_TYPE_BLOB:
    // value = cursor.getBlob(cIndex);
    // break;
    // default:
    // break;
    // }
    // // Log.d(TAG, "coulumnName:" + columnName + ", value:" + value);
    // return value;
    // }
    public static int getInteger(Cursor cursor, String columnName) {

        return cursor.getInt(cursor.getColumnIndex(columnName));
    }

    public static String getString(Cursor cursor, String columnName) {

        return cursor.getString(cursor.getColumnIndex(columnName));
    }

    public static long getLong(Cursor cursor, String columnName) {

        return cursor.getLong(cursor.getColumnIndex(columnName));
    }
}
