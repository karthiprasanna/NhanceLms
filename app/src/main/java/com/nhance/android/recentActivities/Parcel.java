package com.nhance.android.recentActivities;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by prathibha on 5/16/2017.
 */

public class Parcel implements Serializable {
    private JSONObject obj;

    public Parcel(JSONObject obj) {
        this.obj = obj;
    }

    public JSONObject getObj() {
        return obj;
    }}
