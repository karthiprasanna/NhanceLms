package com.nhance.android.db.models;

import com.nhance.android.db.models.entity.AbstractEntity;

public class TestQTypeMetadata extends AbstractEntity {

    /**
     * 
     */
    private static final long serialVersionUID = 8672176336597104032L;
    // name will be treated as questionType
    public String             testId;
    public int                qusCount;
    public int                totalMarks;
    public int                positive;
    public int                negative;
    public String             courseBrdId;
    public String             courseName;
    public String             qids;                                   // comma separated values of
                                                                       // question ids;

    public TestQTypeMetadata() {

        super(null);
    }

    public TestQTypeMetadata(String name, String testId, int qusCount, int totalMarks,
            int positive, int negative, String courseBrdId, String courseName, String qids) {

        super(name);
        this.testId = testId;
        this.qusCount = qusCount;
        this.totalMarks = totalMarks;
        this.positive = positive;
        this.negative = negative;
        this.courseBrdId = courseBrdId;
        this.courseName = courseName;
        this.qids = qids;
    }

}
