package com.nhance.android.assignment.pojo.content.infos;

/*
import android.util.Log;

import com.nhance.android.assignment.TeacherModule.AssignmentResultVisibility;
import com.nhance.android.assignment.pojo.assignment.AssignmentMetadata;
import com.nhance.android.assignment.pojo.assignment.AssignmentQuestionSet;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.enums.AttemptState;
import com.nhance.android.enums.TestResultVisibility;
import com.nhance.android.pojos.content.infos.IContentInfo;
import com.nhance.android.pojos.tests.TestMetadata;
import com.nhance.android.pojos.tests.TestQuestionSet;
import com.nhance.android.utils.JSONUtils;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;



public class AssignmentExtendedInfo implements IContentInfo {

    private static final long    serialVersionUID = 1L;
    public int                   qusCount;
    public int                   duration;
    public int                   totalMarks;
    public String                name;
    public String                code;
    public String                mode;
    public List<AssignmentQuestionSet> sets;
    public List<AssignmentMetadata>    metadata;

    // (if this is testGroup then members of this group)
    public List<String>          childrenIds;

    // if this is part of a testGroup (testGroupId)
    public String                parentId;

    // this info is added at run time, may be we can directly
    // accessed from analytics manager
    public AttemptState          attempteState;

    // this will be used when a test result are hidden from student
    public AssignmentResultVisibility resultVisibility;
    public String                resultVisibilityMessage;

    @SuppressWarnings("unchecked")
    @Override
    public void fromJSON(JSONObject json) {

        qusCount = JSONUtils.getInt(json, ConstantGlobal.QUS_COUNT);
        totalMarks = JSONUtils.getInt(json, ConstantGlobal.TOTAL_MARKS);
        duration = JSONUtils.getInt(json, ConstantGlobal.DURATION);
        name = JSONUtils.getString(json, ConstantGlobal.NAME);
        code = JSONUtils.getString(json, ConstantGlobal.CODE);
        mode = JSONUtils.getString(json, "mode");
        sets = (List<AssignmentQuestionSet>) JSONUtils.getJSONAwareCollection(AssignmentQuestionSet.class,
                json, "sets");
        metadata = (List<AssignmentMetadata>) JSONUtils.getJSONAwareCollection(AssignmentMetadata.class, json,
                "metadata");
        childrenIds = JSONUtils.getList(json, "childrenIds");
        parentId = JSONUtils.getString(json, ConstantGlobal.PARENT_ID);
        resultVisibility = AssignmentResultVisibility.valueOfKey(JSONUtils.getString(json,
                "resultVisibility"));
        resultVisibilityMessage = JSONUtils.getString(json, "resultVisibilityMessage");
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

    public List<String> __getAllQIds() {

        return __getAllQIds(null);
    }

    public List<String> __getAllQIds(String brdId) {

        List<String> qIds = new ArrayList<String>();
        for (AssignmentMetadata mdata : metadata) {
            if (brdId == null || StringUtils.equals(brdId, mdata.id)) {
                if (mdata.qIds != null) {
                    qIds.addAll(mdata.qIds);
                }
            }
        }
        return qIds;
    }

    public Map<String, List<String>> getBoardWiseQidMap() {

        Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();

        for (AssignmentMetadata mdata : metadata) {
            List<String> qIds = new ArrayList<String>();
            if (mdata.qIds != null) {
                qIds.addAll(mdata.qIds);
            }
            map.put(mdata.id, qIds);
        }
        return map;
    }

    public AssignmentQuestionSet __getQuestionSet(String setName) {

        if (setName == null) {
            return null;
        }
        for (AssignmentQuestionSet qSet : sets) {
            if (StringUtils.equalsIgnoreCase(setName, qSet.name)) {
                return qSet;
            }
        }
        return null;
    }

    public AssignmentMetadata __getAssignmentMetadata(String brdId) {

        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        AssignmentMetadata mdata = null;
        for (AssignmentMetadata m : metadata) {
            if (StringUtils.equals(brdId, m.id)) {
                mdata = m;
                break;
            }
        }
        return mdata;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{qusCount:").append(qusCount).append(", duration:").append(duration)
                .append(", totalMarks:").append(totalMarks).append(", code:").append(code)
                .append(", mode:").append(mode).append(", sets:").append(sets)
                .append(", metadata:").append(metadata).append(", childrenIds:")
                .append(childrenIds).append(", parentId:").append(parentId)
                .append(", attempteState:").append(attempteState).append(", resultVisibility:")
                .append(resultVisibility).append(", resultVisibilityMessage:")
                .append(resultVisibilityMessage).append("}");
        return builder.toString();
    }

}





*/


import android.util.Log;

import com.nhance.android.assignment.TeacherModule.AssignmentResultVisibility;
import com.nhance.android.assignment.pojo.assignment.AssignmentMetadata;
import com.nhance.android.assignment.pojo.assignment.AssignmentQuestionSet;
import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.enums.AttemptState;
import com.nhance.android.pojos.content.infos.IContentInfo;
import com.nhance.android.utils.JSONUtils;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//Himank Shah
public class AssignmentExtendedInfo implements IContentInfo {




    private static final long serialVersionUID = 1L;
    public int qusCount;
    public int duration;
    public int totalMarks;
    public String name;
    public String code;
    public String firstName;
    public String mode;
    public List<AssignmentQuestionSet> sets;
    public List<AssignmentMetadata> metadata;

    // (if this is assignmentGroup then members of this group)
    public List<String> childrenIds;

    // if this is part of a assignmentGroup (assignmentGroupId)
    public String parentId;

    // this info is added at run time, may be we can directly
    // accessed from analytics manager
    public AttemptState attempteState;
    private AssignmentResultVisibility resultVisibility;
    private String resultVisibilityMessage;

    // this will be used when a assignment result are hidden from student
//    public AssignmentResultVisibility resultVisibility;
//    public String resultVisibilityMessage;

    @SuppressWarnings("unchecked")
    @Override
    public void fromJSON(JSONObject json) {

        qusCount = JSONUtils.getInt(json, ConstantGlobal.QUS_COUNT);
        totalMarks = JSONUtils.getInt(json, ConstantGlobal.TOTAL_MARKS);
        duration = JSONUtils.getInt(json, ConstantGlobal.DURATION);
        name = JSONUtils.getString(json, ConstantGlobal.NAME);
        firstName = JSONUtils.getString(json, ConstantGlobal.FIRST_NAME);
        code = JSONUtils.getString(json, ConstantGlobal.CODE);
        mode = JSONUtils.getString(json, "mode");
        Log.e("Json json",json.toString());
        sets = (List<AssignmentQuestionSet>) JSONUtils.getJSONAwareCollection(AssignmentQuestionSet.class,
                json, "sets");
        Log.e("Json Sets",sets.toString());
        metadata = (List<AssignmentMetadata>) JSONUtils.getJSONAwareCollection(AssignmentMetadata.class, json,
                "metadata");
        Log.e("Json metadata",metadata.toString());
        childrenIds = JSONUtils.getList(json, "childrenIds");
        parentId = JSONUtils.getString(json, ConstantGlobal.PARENT_ID);
        resultVisibility = AssignmentResultVisibility.valueOfKey(JSONUtils.getString(json,
                "resultVisibility"));
        resultVisibilityMessage = JSONUtils.getString(json, "resultVisibilityMessage");
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

    public List<String> __getAllQIds() {

        return __getAllQIds(null);
    }

    public List<String> __getAllQIds(String brdId) {

        List<String> qIds = new ArrayList<String>();
        for (AssignmentMetadata mdata : metadata) {
            if (brdId == null || StringUtils.equals(brdId, mdata.id)) {
                if (mdata.qIds != null) {
                    qIds.addAll(mdata.qIds);
                }
            }
        }
        return qIds;
    }

    public Map<String, List<String>> getBoardWiseQidMap() {

        Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();

        for (AssignmentMetadata mdata : metadata) {
            List<String> qIds = new ArrayList<String>();
            if (mdata.qIds != null) {
                qIds.addAll(mdata.qIds);
            }
            map.put(mdata.id, qIds);
        }
        return map;
    }

    public AssignmentQuestionSet __getQuestionSet(String setName) {

        if (setName == null) {
            return null;
        }
        for (AssignmentQuestionSet qSet : sets) {
            if (StringUtils.equalsIgnoreCase(setName, qSet.name)) {
                return qSet;
            }
        }
        return null;
    }

    public AssignmentMetadata __getAssignmentMetadata(String brdId) {

        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        AssignmentMetadata mdata = null;
        for (AssignmentMetadata m : metadata) {
            if (StringUtils.equals(brdId, m.id)) {
                mdata = m;
                break;
            }
        }
        return mdata;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{qusCount:").append(qusCount).append(", duration:").append(duration)
                .append(", firstName:").append(firstName).append(", totalMarks:").append(totalMarks).append(", code:").append(code)
                .append(", mode:").append(mode).append(", sets:").append(sets)
                .append(", metadata:").append(metadata).append(", childrenIds:")
                .append(childrenIds).append(", parentId:").append(parentId)
                .append(", attempteState:").append(attempteState)
.append(", resultVisibility:")
                .append(resultVisibility).append(", resultVisibilityMessage:")
                .append(resultVisibilityMessage).append("}")
;
        return builder.toString();
    }

}
