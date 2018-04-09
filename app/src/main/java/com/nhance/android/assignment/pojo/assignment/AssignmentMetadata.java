package com.nhance.android.assignment.pojo.assignment;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.pojos.tests.BoardQus;
import com.nhance.android.pojos.tests.Marks;
import com.nhance.android.utils.JSONAware;
import com.nhance.android.utils.JSONUtils;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Himank Shah on 11/30/2016.
 */

public class AssignmentMetadata implements JSONAware, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public String id;                   // board
    // _id
    public String name;                 // borad
    // name

    public int                qusCount;
public String firstName;
    // qType wise details
    public List<AssignmentDetails> details;

    public int                totalMarks;



    public List<BoardQus> children;

    public List<String> qIds;

    public Map<String, Marks> marks;

    public AssignmentMetadata() {

        super();
    }

    public AssignmentMetadata(String id, String name, int qusCount) {

        this.id = id;
        this.name = name;
        this.qusCount = qusCount;
        this.qIds = new ArrayList<String>();
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

    @Override
    public void fromJSON(JSONObject json) {

        id = JSONUtils.getString(json, ConstantGlobal.ID);
        name = JSONUtils.getString(json, ConstantGlobal.NAME);
        qusCount = JSONUtils.getInt(json, ConstantGlobal.QUS_COUNT);
        totalMarks = JSONUtils.getInt(json, ConstantGlobal.TOTAL_MARKS);
        qIds = JSONUtils.getList(json, ConstantGlobal.QIDS);
        JSONArray childrenJSONArray = JSONUtils.getJSONArray(json, "children");
        if (childrenJSONArray != null) {
            if (children == null) {
                children = new ArrayList<BoardQus>();
            }
            for (int i = 0; i < childrenJSONArray.length(); i++) {
                BoardQus bQ = new BoardQus();
                JSONObject childrenJSON;
                try {
                    childrenJSON = childrenJSONArray.getJSONObject(i);
                } catch (JSONException e) {
                    continue;
                }
                bQ.fromJSON(childrenJSON);
                children.add(bQ);
            }
        }

        JSONArray detailsJSONArray = JSONUtils.getJSONArray(json, "details");
        if (detailsJSONArray != null) {
            if (details == null) {
                details = new ArrayList<AssignmentDetails>();
            }
            for (int i = 0; i < detailsJSONArray.length(); i++) {
                AssignmentDetails detail = new AssignmentDetails();

                JSONObject detailsJSON;
                try {
                    detailsJSON = detailsJSONArray.getJSONObject(i);
                } catch (JSONException e) {
                    continue;
                }
                detail.fromJSON(detailsJSON);

                details.add(detail);
            }
        }
        if (marks == null) {
            marks = new HashMap<String, Marks>();
        }
//        JSONObject mJSON = JSONUtils.getJSONObject(json, "marks");
//        @SuppressWarnings("unchecked")
//        Iterator<String> it = mJSON.keys();
//        while (it.hasNext()) {
//            String key = it.next();
//            Marks m = new Marks();
//            m.fromJSON(JSONUtils.getJSONObject(mJSON, key));
//            marks.put(key, m);
//        }
    }

    @Override
    public int hashCode() {

        return ((id == null) ? 0 : id.hashCode());
    }

    public Marks getMarks(String qId) {

        if (marks == null || marks.isEmpty()) {
            finishEditing();
        }
        return marks.get(qId);
    }

    public void finishEditing() {

        if (marks == null) {
            marks = new HashMap<String, Marks>();
        }
        if (details == null) {
            return;
        }

        for (AssignmentDetails detail : details) {
            if (detail.qIds != null) {
                for (String qId : detail.qIds) {
                    marks.put(qId, detail.marks);
                }
            }
        }
    }
//karthi
    @Override
    public boolean equals(Object obj) {

        if (obj == null || getClass() != obj.getClass())
            return false;
        AssignmentMetadata other = (AssignmentMetadata) obj;
        return StringUtils.equals(id, other.id);
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{id:").append(id).append(", name:").append(name).append(", qusCount:")
                .append(qusCount).append(", details:").append(details).append(", totalMarks:")
                .append(totalMarks).append(", children:").append(children).append(", qIds:")
                .append(qIds).append(", marks:").append(marks).append("}");
        return builder.toString();
    }

}
