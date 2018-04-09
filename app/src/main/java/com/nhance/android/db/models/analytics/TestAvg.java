package com.nhance.android.db.models.analytics;

import java.io.Serializable;

/**
 * Created by Himank Shah on 7/24/2017.
 */

public class TestAvg implements Serializable {
    public String totalMarks;
    public String score;
    public String attempted;

    public TestAvg() {
    }

    public TestAvg(String totalMarks, String score, String attempted) {
        this.totalMarks = totalMarks;
        this.score = score;
        this.attempted = attempted;

    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{totalMarks:").append(totalMarks).append(", score:").append(score).append(", attempted:")
                .append(attempted).append("}");
        return builder.toString();
    }

}
