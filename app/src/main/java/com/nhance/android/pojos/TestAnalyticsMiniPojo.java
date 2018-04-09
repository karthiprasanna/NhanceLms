package com.nhance.android.pojos;

import java.io.Serializable;

import com.nhance.android.interfaces.IAnalyticsInfoPojo;

public class TestAnalyticsMiniPojo implements Serializable, IAnalyticsInfoPojo {

    /**
     * 
     */
    private static final long serialVersionUID = -2203075818043853993L;
    public int                contentId;
    public String             name;
    public String             id;
    public int                percentage;
    public int                noOfCourses;
    public int                score;
    public int                totalMarks;
    public long               timeAttempted;

    public TestAnalyticsMiniPojo(int contentId, String id, String name, int percentage, int score,
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
