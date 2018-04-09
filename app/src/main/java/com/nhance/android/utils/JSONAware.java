package com.nhance.android.utils;

import java.io.Serializable;

import org.json.JSONObject;

public interface JSONAware extends Serializable {

    public void fromJSON(JSONObject json);

    public JSONObject toJSON();
}
