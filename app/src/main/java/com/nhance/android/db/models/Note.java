package com.nhance.android.db.models;

import com.nhance.android.db.models.entity.AbstractVEntity;
import com.nhance.android.enums.NoteType;

public class Note extends AbstractVEntity {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    // name will represent the name/title of the entity
    public String             userId;
    public String             noteType;
    public String             desc;
    public String             progId;
    public String             entityId;
    public String             entityType;
    public String             thumb;                // thumbnail of the content
    public String             contentType;
    public String             courseBrdName;
    public String             courseBrdId;
    public String             lastViewed;

    public Note() {
        super(null);
    }

    public Note(String name, String userId, String progId, NoteType noteType, String entityId,
            String entityType, String contentType, String thumb, String desc, String courseBrdName,
            String courseBrdId, String by) {

        super(name);
        this.by = by;
        this.userId = userId;
        this.progId = progId;
        this.noteType = noteType.name();
        this.entityId = entityId;
        this.entityType = entityType;
        this.contentType = contentType;
        this.thumb = thumb;
        this.desc = desc;
        this.courseBrdName = courseBrdName;
        this.courseBrdId = courseBrdId;
        this.lastViewed = String.valueOf(System.currentTimeMillis());
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{userId:").append(userId).append(", noteType:").append(noteType)
                .append(", desc:").append(desc).append(", progId:").append(progId)
                .append(", entityId:").append(entityId).append(", entityType:").append(entityType)
                .append(", thumb:").append(thumb).append(", contentType:").append(contentType)
                .append(", courseBrdName:").append(courseBrdName).append(", courseBrdId:")
                .append(courseBrdId).append(", lastViewed:").append(lastViewed).append(", by:")
                .append(by).append(", name:").append(name).append(", _id:").append(_id)
                .append(", timeCreated:").append(timeCreated).append("}");
        return builder.toString();
    }

}
