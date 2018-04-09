package com.nhance.android.db.models.entity;

import org.json.JSONObject;

import com.nhance.android.QuestionCount.QuestionBasicInfo;
import com.nhance.android.QuestionCount.QuestionExtendedInfo;
import com.nhance.android.assignment.pojo.content.infos.AssignmentExtendedInfo;
import com.nhance.android.enums.EntityType;
import com.nhance.android.pojos.content.infos.AssignmentBasicInfo;

import com.nhance.android.pojos.content.infos.DocumentBasicInfo;
import com.nhance.android.pojos.content.infos.DocumentExtendedInfo;
import com.nhance.android.pojos.content.infos.FileBasicInfo;
import com.nhance.android.pojos.content.infos.FileExtendedInfo;
import com.nhance.android.pojos.content.infos.HtmlContentBasicInfo;
import com.nhance.android.pojos.content.infos.HtmlContentExtendedInfo;
import com.nhance.android.pojos.content.infos.IContentInfo;
import com.nhance.android.pojos.content.infos.ModuleBasicInfo;
import com.nhance.android.pojos.content.infos.ModuleExtendedInfo;
import com.nhance.android.pojos.content.infos.TestBasicInfo;
import com.nhance.android.pojos.content.infos.TestExtendedInfo;
import com.nhance.android.pojos.content.infos.VideoBasicInfo;
import com.nhance.android.pojos.content.infos.VideoExtendedInfo;

public class Content extends ContentBoardEntity {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public String id;                   // entityId
    public String type;                 // type of content(entityType)

    public String name;
    public String desc;

    public JSONObject info;
    public String subType;

    public String thumb;                // optional
    public String file;                 // name of the file if needed

    public Content() {

        super();
    }

    @Override
    public boolean equalsIgnoreCase(String attempted) {
        return false;
    }

    public Content(String timeCreated, int orgKeyId, String lastUpdated, String brdIds,
                   String tags, String userId, String ownerId, String ownerName, String id, String type,
                   String name, String desc, JSONObject info, String subType, String thumb, String file) {

        super(timeCreated, orgKeyId, lastUpdated, brdIds, tags);
        this.userId = userId;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.id = id;
        this.type = type;
        this.name = name;
        this.desc = desc;
        this.info = info;
        this.subType = subType;
        this.thumb = thumb;
        this.file = file;
    }

    IContentInfo cBasicInfo = null;

    public IContentInfo toContentBasicInfo() {

        if (cBasicInfo != null) {
            return cBasicInfo;
        }
        EntityType eType = EntityType.valueOfKey(type);
        switch (eType) {
            case TEST:
                cBasicInfo = new TestBasicInfo();
                break;
            case ASSIGNMENT:
                cBasicInfo = new AssignmentBasicInfo();
                break;
            case DOCUMENT:
                cBasicInfo = new DocumentBasicInfo();
                break;
            case VIDEO:
                cBasicInfo = new VideoBasicInfo();
                break;
            case FILE:
                cBasicInfo = new FileBasicInfo();
                break;
            case MODULE:
                cBasicInfo = new ModuleBasicInfo();
                break;
            case HTMLCONTENT:
                cBasicInfo = new HtmlContentBasicInfo();
                break;
            case QUESTION:
                cBasicInfo = new QuestionBasicInfo();
                break;


            default:
                break;
        }
        if (cBasicInfo != null) {
            cBasicInfo.fromJSON(info);
        }
        return cBasicInfo;
    }

    IContentInfo cExtendedInfo = null;

    public IContentInfo toContentExtendedInfo() {

        if (cExtendedInfo != null) {
            return cExtendedInfo;
        }

        EntityType eType = EntityType.valueOfKey(type);
        switch (eType) {
            case TEST:
                cExtendedInfo = new TestExtendedInfo();
                break;
            case ASSIGNMENT:
                cExtendedInfo = new AssignmentExtendedInfo();
                break;
            case DOCUMENT:
                cExtendedInfo = new DocumentExtendedInfo();
                break;
            case VIDEO:
                cExtendedInfo = new VideoExtendedInfo();
                break;
            case FILE:
                cExtendedInfo = new FileExtendedInfo();
                break;
            case MODULE:
                cExtendedInfo = new ModuleExtendedInfo();
                // TODO: add annotate module info
                break;
            case HTMLCONTENT:
                cExtendedInfo = new HtmlContentExtendedInfo();
                break;
            case QUESTION:
                cExtendedInfo = new QuestionExtendedInfo();
                break;
            default:
                break;
        }
        if (cExtendedInfo != null) {
            cExtendedInfo.fromJSON(info);
        }
        return cExtendedInfo;
    }

    public EntityType __getEntityType() {

        return EntityType.valueOfKey(type);
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{id:").append(id).append(", type:").append(type).append(", name:")
                .append(name).append(", desc:").append(desc).append(", info:").append(info)
                .append(", subType:").append(subType).append(", thumb:").append(thumb)
                .append(", file:").append(file).append(", userId:").append(userId)
                .append(", ownerId:").append(ownerId).append(", ownerName:").append(ownerName)
                .append(", lastUpdated:").append(lastUpdated).append(", downloaded:")
                .append(downloaded).append(", starred:").append(starred).append(", lastViewed:")
                .append(lastViewed).append(", brdIds:").append(brdIds).append(", tags:")
                .append(tags).append(", targetIds:").append(targetIds).append(", targetNames:")
                .append(targetNames).append(", _id:").append(_id).append(", orgKeyId:")
                .append(orgKeyId).append(", timeCreated:").append(timeCreated).append("}");
        return builder.toString();
    }

}
