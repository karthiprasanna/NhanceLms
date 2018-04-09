package com.nhance.android.assignment.pojo;

import com.nhance.android.interfaces.IAnalyticsInfoPojo;

import java.io.Serializable;

/**
 * Created by Himank Shah on 12/7/2016.
 */

public class AssignmentAnalyticsMiniPojo implements Serializable, IAnalyticsInfoPojo {

    /**
     *
     */
    private static final long serialVersionUID = -2203075818043853993L;
    public int                contentId;
    public String name;
    public String id;
    public int                percentage;
    public int                noOfCourses;
    public int                score;
    public int                totalMarks;
    public long               timeAttempted;

    public AssignmentAnalyticsMiniPojo(int contentId, String id, String name, int percentage, int score,
                                       int totalMarks, int noOfCourses, long timeAttempted) {

        this.contentId = contentId;
        this.id = id;
        this.name = name;
        this.percentage = percentage;
        this.score = score;
        this.totalMarks = totalMarks;
        this.noOfCourses = noOfCourses;
        this.timeAttempted = timeAttempted;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append(name);
        return builder.toString();
    }

}
