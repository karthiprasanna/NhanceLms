package com.nhance.android.db.models;

import org.json.JSONObject;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.utils.JSONAware;
import com.nhance.android.utils.JSONUtils;

public class SDCardFileMetadata extends AbstractDataModel implements JSONAware {

    /**
     * 
     */
    private static final long  serialVersionUID = 1L;
    public static final String FIELD_LOCATION   = ConstantGlobal.LOCATION;
    public static final String FIELD_SIZE       = "size";
    public static final String FIELD_CARD_ID    = "cardId";
    public static final String FIELD_CARD_NAME  = "cardName";
    public static final String FIELD_ACTIVE     = "active";
    public static final String FIELD_MOUNT_PATH = "mountPath";

    public String              userId;
    public String              name;

    public String              targetId;
    public String              targetType;

    public String              entityId;
    public String              entityType;

    public String              location;                                  // Location relative to
                                                                           // the card
    public long                size;
    public String              cardId;
    public String              cardName;
    public boolean             active;

    public String              mountPath;                                 // mount path

    public SDCardFileMetadata() {

        super();
    }

    public SDCardFileMetadata(int orgKeyId, String userId, String targetId, String targetType,
            String entityId, String entityType, String name, String location, long size,
            String cardId, String cardName) {

        super(orgKeyId);
        this.userId = userId;
        this.targetId = targetId;
        this.targetType = targetType;
        this.entityId = entityId;
        this.entityType = entityType;
        this.name = name;
        this.location = location;
        this.size = size;
        this.cardId = cardId;
        this.cardName = cardName;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{userId:").append(userId).append(", name:").append(name)
                .append(", entityId:").append(entityId).append(", entityType:").append(entityType)
                .append(", location:").append(location).append(", size:").append(size)
                .append(", cardId:").append(cardId).append(", cardName:").append(cardName)
                .append(", active:").append(active).append(", mountPath:").append(mountPath)
                .append(", _id:").append(_id).append(", orgKeyId:").append(orgKeyId)
                .append(", timeCreated:").append(timeCreated).append("}");
        return builder.toString();
    }

    @Override
    public void fromJSON(JSONObject json) {

        entityId = JSONUtils.getString(json, ConstantGlobal.ENTITY_ID);
        entityType = JSONUtils.getString(json, ConstantGlobal.ENTITY_TYPE);
        name = JSONUtils.getString(json, ConstantGlobal.NAME);
        location = JSONUtils.getString(json, FIELD_LOCATION);
        size = JSONUtils.getLong(json, FIELD_SIZE);
        cardId = JSONUtils.getString(json, FIELD_CARD_ID);
        cardName = JSONUtils.getString(json, FIELD_CARD_NAME);
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

}
