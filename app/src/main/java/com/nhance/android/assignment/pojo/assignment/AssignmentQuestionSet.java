package com.nhance.android.assignment.pojo.assignment;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.utils.JSONAware;
import com.nhance.android.utils.JSONUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Himank Shah on 11/30/2016.
 */

public class AssignmentQuestionSet implements JSONAware {

    public String name;
    public List<String> qIds;

    public AssignmentQuestionSet() {

        super();
    }

    public AssignmentQuestionSet(String name) {

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

        if (name == null || obj == null || !(obj instanceof AssignmentQuestionSet)) {
            return false;
        }
        return ((AssignmentQuestionSet) obj).name.equalsIgnoreCase(name);
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
