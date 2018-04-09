package com.nhance.android.db.models.entity;

import org.json.JSONArray;

import com.nhance.android.db.models.AbstractDataModel;

public class ContentLink extends AbstractDataModel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public String             linkId;
    public String             userId;               // for userId
    // public String ownerId; // content added by
    // public String ownerName; // content added by name

    public String             entityId;
    public String             entityType;
    public String             targetId;             // target sectionId
    public String             targetType;           // target type (i.e section)
    public String             lastUpdated;
    public boolean            downloadable;
    public String             encLevel;

    // default order
    public long               position;

    // encryption details
    public String             passphrase;
    public JSONArray          downloadableEntities;

    public ContentLink() {

        super();
    }

    public ContentLink(String linkId, int orgKeyId, String timeCreated, String lastUpdated,
            String userId, String entityId, String entityType, String targetId, String targetType,
            boolean downloadable, JSONArray downloadableEntities, long position) {

        super(timeCreated, orgKeyId);
        this.linkId = linkId;
        this.userId = userId;
        this.entityId = entityId;
        this.entityType = entityType;
        this.targetId = targetId;
        this.targetType = targetType;
        this.lastUpdated = lastUpdated;
        this.downloadable = downloadable;
        this.downloadableEntities = downloadableEntities;
        this.position = position;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{linkId:").append(linkId).append(", userId:").append(userId)
                .append(", entityId:").append(entityId).append(", entityType:").append(entityType)
                .append(", targetId:").append(targetId).append(", targetType:").append(targetType)
                .append(", lastUpdated:").append(lastUpdated).append(", downloadable:")
                .append(downloadable).append(", encLevel:").append(encLevel).append(", position:")
                .append(position).append(", passphrase:").append(passphrase)
                .append(", downloadableEntities:").append(downloadableEntities).append(", _id:")
                .append(_id).append(", orgKeyId:").append(orgKeyId).append(", timeCreated:")
                .append(timeCreated).append("}");
        return builder.toString();
    }

}
