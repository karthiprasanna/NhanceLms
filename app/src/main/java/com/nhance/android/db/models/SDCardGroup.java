package com.nhance.android.db.models;

public class SDCardGroup extends AbstractDataModel {

    /**
     * 
     */
    private static final long  serialVersionUID     = 1L;
    public static final String FIELD_ACTIVATED_TIME = "activatedTime";
    public static final String FIELD_ACCESS_CODE    = "accessCode";
    public static final String FIELD_ACTIVATED      = "activated";

    public String              name;
    public String              id;
    public long                size;
    public String              userId;

    public String              targetId;
    public String              targetType;

    public long                activatedTime;
    public String              accessCode;
    public boolean             activated;

    public SDCardGroup() {

        super();
    }

    public SDCardGroup(int orgKeyId, String userId, String name, String id, long size,
            String targetId, String targetType) {

        super(orgKeyId);
        this.name = name;
        this.userId = userId;
        this.id = id;
        this.size = size;
        this.targetId = targetId;
        this.targetType = targetType;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{name:").append(name).append(", id:").append(id).append(", size:")
                .append(size).append(", userId:").append(userId).append(", targetId:")
                .append(targetId).append(", targetType:").append(targetType)
                .append(", activatedTime:").append(activatedTime).append(", accessCode:")
                .append(accessCode).append(", activated:").append(activated).append(", _id:")
                .append(_id).append(", orgKeyId:").append(orgKeyId).append(", timeCreated:")
                .append(timeCreated).append("}");
        return builder.toString();
    }

}
