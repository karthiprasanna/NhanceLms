package com.nhance.android.assignment.db.models.analytics;

import com.nhance.android.utils.SQLDBUtil;

import java.io.Serializable;

/**
 * Created by Himank Shah on 12/6/2016.
 */

public class AssignmentBoardAnalytics extends AbstractAssignmentAnalytics implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public String id;
    public String type;
    public String name;
    public String parentId;

    public AssignmentBoardAnalytics() {

        super(SQLDBUtil.NO_INT_VALUE, null, SQLDBUtil.NO_INT_VALUE, SQLDBUtil.NO_INT_VALUE, null,
                null, SQLDBUtil.NO_INT_VALUE, SQLDBUtil.NO_INT_VALUE, SQLDBUtil.NO_INT_VALUE,
                SQLDBUtil.NO_INT_VALUE);
    }

    public AssignmentBoardAnalytics(int orgKeyId, String userId, int score, float timeTaken,
                                    String entityId, String entityType, int totalMarks, int qusCount, int attempted,
                                    int correct, String id, String type, String name, String parentId) {

        super(orgKeyId, userId, score, timeTaken, entityId, entityType, totalMarks, qusCount,
                attempted, correct);
        this.id = id;
        this.type = type;
        this.name = name;
        this.parentId = parentId;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{id:").append(id).append(", type:").append(type).append(", name:")
                .append(name).append(", parentId:").append(parentId).append(", entityId:")
                .append(entityId).append(", entityType:").append(entityType)
                .append(", totalMarks:").append(totalMarks).append(", qusCount:").append(qusCount)
                .append(", attempted:").append(attempted).append(", correct:").append(correct)
                .append(", userId:").append(userId).append(", score:").append(score)
                .append(", timeTaken:").append(timeTaken).append(", _id:").append(_id)
                .append(", orgKeyId:").append(orgKeyId).append(", timeCreated:")
                .append(timeCreated).append("}");
        return builder.toString();
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        AssignmentBoardAnalytics other = (AssignmentBoardAnalytics) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

}
