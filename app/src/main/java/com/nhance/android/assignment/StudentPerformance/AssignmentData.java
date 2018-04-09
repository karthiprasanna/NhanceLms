package com.nhance.android.assignment.StudentPerformance;

/**
 * Created by administrator on 4/13/17.
 */

public class AssignmentData {

    public String solutions;
    public String type;
    public String content;

    @Override
    public String toString() {
        return "AssignmentData{" +
                "solutions='" + solutions + '\'' +
                ", type='" + type + '\'' +
                ", content='" + content + '\'' +
                ", status='" + status + '\'' +
                ", id='" + id + '\'' +
                ", Checked=" + Checked +
                '}';
    }

    public String status;

    public String id;
    public  int Checked;

}
