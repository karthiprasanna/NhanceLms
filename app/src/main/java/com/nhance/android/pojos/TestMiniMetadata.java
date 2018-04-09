package com.nhance.android.pojos;

import java.io.Serializable;

public class TestMiniMetadata implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4663245880799544299L;
    public String name;//testName
    public String testId;
    public int duration;// in seconds
    public int totalMarks;
    public int qusCount;
    public String by;
    public boolean attempted;

    public TestMiniMetadata() {
    }

    public TestMiniMetadata(String name, String testId, int duration, int totalMarks,
            int qusCount, String by, boolean attempted) {
        this.name = name;
        this.testId = testId;
        this.duration = duration;
        this.totalMarks = totalMarks;
        this.qusCount = qusCount;
        this.by = by;
        this.attempted = attempted;
    }

}
