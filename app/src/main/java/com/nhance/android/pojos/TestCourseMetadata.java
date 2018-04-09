package com.nhance.android.pojos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.nhance.android.db.models.TestQTypeMetadata;
import com.nhance.android.db.models.TestTopicMetadata;

public class TestCourseMetadata implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 7371806075521403540L;
    public String courseName;
    public String courseBrdId;
    public int totalMarks;
    public int qusCount;

    public List<TestQTypeMetadata> qTypesMetadata;
    public List<TestTopicMetadata> topicsMetadata;

    public void addTopicMedatadat(TestTopicMetadata topicMetadata) {
        if (topicsMetadata == null) {
            topicsMetadata = new ArrayList<TestTopicMetadata>();
        }
        topicsMetadata.add(topicMetadata);
    }

    public void addQTypeMetadata(TestQTypeMetadata qTypeMetadata) {
        if (qTypesMetadata == null) {
            qTypesMetadata = new ArrayList<TestQTypeMetadata>();
        }
        qTypesMetadata.add(qTypeMetadata);
    }

    public void processQTypeData() {
        if (qTypesMetadata != null && !qTypesMetadata.isEmpty()) {
            courseName = qTypesMetadata.get(0).courseName;
            courseBrdId = qTypesMetadata.get(0).courseBrdId;
            for (TestQTypeMetadata qTypeData : qTypesMetadata) {
                totalMarks += qTypeData.totalMarks;
                qusCount += qTypeData.qusCount;
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((courseBrdId == null) ? 0 : courseBrdId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TestCourseMetadata other = (TestCourseMetadata) obj;
        if (courseBrdId == null) {
            if (other.courseBrdId != null)
                return false;
        } else if (!courseBrdId.equals(other.courseBrdId))
            return false;
        return true;
    }

}
