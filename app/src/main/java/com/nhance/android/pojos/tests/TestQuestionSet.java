package com.nhance.android.pojos.tests;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.utils.JSONAware;
import com.nhance.android.utils.JSONUtils;

public class TestQuestionSet implements JSONAware {

    public String       name;
    public List<String> qIds;

    public TestQuestionSet() {

        super();
    }

    public TestQuestionSet(String name) {

        this.name = name;
    }

    public void addQid(String qId) {

        if (qIds == null) {
            qIds = new ArrayList<String>();
        }
        if (!qIds.contains(qId)) {
            qIds.add(qId);
        }
    }

    @Override
    public boolean equals(Object obj) {

        if (name == null || obj == null || !(obj instanceof TestQuestionSet)) {
            return false;
        }
        return ((TestQuestionSet) obj).name.equalsIgnoreCase(name);
    }

    @Override
    public int hashCode() {

        return name == null ? 0 : name.hashCode();
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{name:").append(name).append(", qIds:").append(qIds).append("}");
        return builder.toString();
    }

    @Override
    public void fromJSON(JSONObject json) {

        name = JSONUtils.getString(json, ConstantGlobal.NAME);
        qIds = JSONUtils.getList(json, ConstantGlobal.QIDS);
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }
}
