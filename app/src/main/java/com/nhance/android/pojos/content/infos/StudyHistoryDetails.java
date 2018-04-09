package com.nhance.android.pojos.content.infos;

import com.nhance.android.db.models.StudyHistory;
import com.nhance.android.enums.EntityType;

public class StudyHistoryDetails {

    public StudyHistory studyHistory;
    public String       name;
    public String       entityId;
    public EntityType   entityType;

    public StudyHistoryDetails(StudyHistory studyHistory, String name, String entityId,
            EntityType entityType) {

        super();
        this.studyHistory = studyHistory;
        this.name = name;
        this.entityId = entityId;
        this.entityType = entityType;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{studyHistory:").append(studyHistory).append(", name:").append(name)
                .append(", entityId:").append(entityId).append(", entityType:").append(entityType)
                .append("}");
        return builder.toString();
    }

}
