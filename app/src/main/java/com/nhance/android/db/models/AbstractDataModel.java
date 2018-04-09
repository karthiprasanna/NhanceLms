package com.nhance.android.db.models;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ContentValues;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.pojos.IListResponseObj;

public abstract class AbstractDataModel implements Serializable, IListResponseObj {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public int                _id;
    public int                orgKeyId;
    public String             timeCreated;

    public AbstractDataModel() {

        this.timeCreated = String.valueOf(Calendar.getInstance().getTimeInMillis());
    }

    public AbstractDataModel(int orgKeyId) {

        this(String.valueOf(Calendar.getInstance().getTimeInMillis()), orgKeyId);
    }

    public AbstractDataModel(String timeCreated, int orgKeyId) {

        super();
        this.timeCreated = timeCreated;
        this.orgKeyId = orgKeyId;
    }

    public ContentValues toContentValues() throws Exception {

        ContentValues values = new ContentValues();
        Class<?> clazz = this.getClass();
        for (Field f : clazz.getFields()) {
            if (Modifier.isStatic(f.getModifiers())) {
                continue;
            }
            Class<?> fClass = f.getType();

            if (fClass.isPrimitive()) {
                if (int.class.equals(fClass)) {
                    values.put(f.getName(), f.getInt(this));
                } else if (long.class.equals(fClass)) {
                    values.put(f.getName(), f.getLong(this));
                } else if (double.class.equals(fClass)) {
                    values.put(f.getName(), f.getDouble(this));
                } else if (float.class.equals(fClass)) {
                    values.put(f.getName(), f.getFloat(this));
                } else if (boolean.class.equals(fClass)) {
                    values.put(f.getName(), f.getBoolean(this) ? 1 : 0);
                }
            } else if (String.class.equals(fClass)) {
                values.put(f.getName(), (String) f.get(this));
            } else if (byte[].class.equals(fClass)) {
                values.put(f.getName(), (byte[]) f.get(this));
            } else if (JSONObject.class.equals(fClass)) {
                values.put(f.getName(), f.get(this).toString());
            } else if (JSONArray.class.equals(fClass)) {
                values.put(f.getName(), f.get(this).toString());
            }
        }
        values.remove(ConstantGlobal._ID);
        return values;
    }
}
