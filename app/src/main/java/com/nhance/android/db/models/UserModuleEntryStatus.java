package com.nhance.android.db.models;

public class UserModuleEntryStatus extends AbstractDataModel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public String             userId;
    public String             moduleId;
    public String             entityId;
    public String             entityType;
    public boolean            synced;

    public UserModuleEntryStatus() {

        super();
    }

    public UserModuleEntryStatus(int orgKeyId, String userId, String moduleId, String entityId,
            String entityType) {

        super(orgKeyId);
        this.userId = userId;
        this.moduleId = moduleId;
        this.entityId = entityId;
        this.entityType = entityType;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{userId:").append(userId).append(", moduleId:").append(moduleId)
                .append(", entityId:").append(entityId).append(", entityType:").append(entityType)
                .append(", _id:").append(_id).append(", orgKeyId:").append(orgKeyId)
                .append(", timeCreated:").append(timeCreated).append("}");
        return builder.toString();
    }

}
