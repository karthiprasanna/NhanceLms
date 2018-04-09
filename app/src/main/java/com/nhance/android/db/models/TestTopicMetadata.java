package com.nhance.android.db.models;

import com.nhance.android.db.models.entity.AbstractEntity;

public class TestTopicMetadata extends AbstractEntity {

    /**
     * 
     */
    private static final long serialVersionUID = 4077742251754619410L;
    public String             testId;
    public String             topicBrdId;
    public String             courseBrdId;
    public int                totalMarks;
    public int                qusCount;
    public String             qids;                                   // comma separated id of
                                                                       // question;

    public TestTopicMetadata() {

        super(null);
    }

    public TestTopicMetadata(String name, String testId, String topicBrdId, String courseBrdId,
            int totalMarks, int qusCount, String qids) {

        super(name);
        this.testId = testId;
        this.topicBrdId = topicBrdId;
        this.courseBrdId = courseBrdId;
        this.totalMarks = totalMarks;
        this.qusCount = qusCount;
        this.qids = qids;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{testId:").append(testId).append(", topicBrdId:").append(topicBrdId)
                .append(", courseBrdId:").append(courseBrdId).append(", totalMarks:")
                .append(totalMarks).append(", qusCount:").append(qusCount).append(", qids:")
                .append(qids).append(", name:").append(name).append(", _id:").append(_id)
                .append(", timeCreated:").append(timeCreated).append("}");
        return builder.toString();
    }

}
