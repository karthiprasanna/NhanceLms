package com.nhance.android.pojos.content.sdcards;

import java.util.List;

import org.json.JSONObject;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.readers.SDCardReader.ActiveGroupInfo;
import com.nhance.android.utils.JSONUtils;

public class SDCardGroupInfo extends AbstractSDCardInfo {

    /**
     * 
     */
    private static final long  serialVersionUID  = 1L;

    public static final String FIELD_SIZE        = "size";
    public static final String FIELD_CARD_SIZE   = "cardSize";
    public static final String FIELD_NO_OF_CARDS = "noOfCards";
    public static final String FIELD_CARD_IDS    = "cardIds";

    public String              targetId;
    public String              targetType;
    public long                cardSize;                       // in bytes
    public int                 noOfCards;
    public List<String>        cardIds;
    public String              orgId;

    // this value will be set by checking and verifying against local database
    public boolean             isActivated;
    public boolean             isPartOfProgramSection;
    public String              mountPath;
    public SDCardInfo          cardInfo;
    public ActiveGroupInfo     activeGroupInfo;

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);
        targetId = JSONUtils.getString(json, ConstantGlobal.TARGET_ID);
        targetType = JSONUtils.getString(json, ConstantGlobal.TARGET_TYPE);
        cardSize = JSONUtils.getLong(json, FIELD_CARD_SIZE);
        noOfCards = JSONUtils.getInt(json, FIELD_NO_OF_CARDS);
        cardIds = JSONUtils.getList(json, FIELD_CARD_IDS);
        orgId = JSONUtils.getString(json, ConstantGlobal.ORG_ID);
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{targetId:").append(targetId).append(", targetType:").append(targetType)
                .append(", cardSize:").append(cardSize).append(", noOfCards:").append(noOfCards)
                .append(", cardIds:").append(cardIds).append(", orgId:").append(orgId)
                .append(", isActivated:").append(isActivated).append(", isPartOfProgramSection:")
                .append(isPartOfProgramSection).append(", cardInfo:").append(cardInfo)
                .append(", name:").append(name).append(", id:").append(id).append(", size:")
                .append(size).append("}");
        return builder.toString();
    }

}
