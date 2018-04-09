package com.nhance.android.assignment.pojo.assignment;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.enums.QuestionType;
import com.nhance.android.pojos.tests.Marks;
import com.nhance.android.utils.JSONAware;
import com.nhance.android.utils.JSONUtils;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Himank Shah on 12/2/2016.
 */

public class AssignmentDetails implements JSONAware, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public QuestionType type;
    public int                qusCount;
    public Marks marks;                // this is temp
    public List<String> qIds;

    public AssignmentDetails() {

        super();
    }

    public AssignmentDetails(QuestionType type, int qusCount, int positiveMarks, int negativeMarks) {

        super();
        this.type = type;
        this.qusCount = qusCount;
        this.marks = new Marks(positiveMarks, negativeMarks);
        this.qIds = new ArrayList<String>();
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

    @Override
    public void fromJSON(JSONObject json) {

        type = QuestionType.valueOfKey(JSONUtils.getString(json, ConstantGlobal.TYPE));
        qusCount = JSONUtils.getInt(json, ConstantGlobal.QUS_COUNT);

//        marks = new Marks(0, 0);
//        JSONObject jsonMarks = JSONUtils.getJSONObject(json, "marks");
//        marks.fromJSON(jsonMarks);

        qIds = JSONUtils.getList(json, ConstantGlobal.QIDS);
    }

    @Override
    public int hashCode() {

        return ((type == null) ? 0 : type.hashCode());
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null || getClass() != obj.getClass())
            return false;
        AssignmentDetails other = (AssignmentDetails) obj;
        return type == other.type;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{type:").append(type).append(", qusCount:").append(qusCount)
                .append(", marks:").append(marks).append(", qIds:").append(qIds).append("}");
        return builder.toString();
    }

}
