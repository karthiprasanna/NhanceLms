package com.nhance.android.assignment.TeacherModule;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by administrator on 10/11/17.
 */

public class QuestionStatusInfo {

    public String totalHits;
    public String qusNo;
    public String solutions;
    public String type;
    public String content;
    public JSONArray options;
    public JSONObject measures;
    public int correct;
    public int incorrect;
    public int left;
    public String attempts;
    public String status;
    public String id;

    @Override
    public String toString() {
        return "QuestionStatusInfo{" +
                "totalHits='" + totalHits + '\'' +
                ", qusNo='" + qusNo + '\'' +
                ", solutions='" + solutions + '\'' +
                ", type='" + type + '\'' +
                ", content='" + content + '\'' +
                ", options=" + options +
                ", measures=" + measures +
                ", correct=" + correct +
                ", incorrect=" + incorrect +
                ", left=" + left +
                ", attempts='" + attempts + '\'' +
                ", status='" + status + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
