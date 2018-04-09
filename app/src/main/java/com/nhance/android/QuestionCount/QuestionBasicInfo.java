package com.nhance.android.QuestionCount;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.pojos.content.infos.IContentInfo;
import com.nhance.android.utils.JSONUtils;

import org.json.JSONObject;


public class QuestionBasicInfo implements IContentInfo {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public int qusCount;
    public int duration;
    public int totalMarks;

    @Override
    public void fromJSON(JSONObject json) {

        qusCount = JSONUtils.getInt(json, ConstantGlobal.QUS_COUNT);
        duration = JSONUtils.getInt(json, ConstantGlobal.DURATION);
        totalMarks = JSONUtils.getInt(json, ConstantGlobal.TOTAL_MARKS);
    }

    @Override
    public JSONObject toJSON() {
        return null;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{qusCount:").append(qusCount).append(", duration:").append(duration)
                .append(", totalMarks:").append(totalMarks).append("}");
        return builder.toString();
    }

}