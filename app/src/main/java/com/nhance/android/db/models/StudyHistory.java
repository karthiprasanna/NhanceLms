package com.nhance.android.db.models;

public class StudyHistory extends AbstractDataModel {

    /**
     * 
     */
    private static final long serialVersionUID = -1202069095371802913L;
    public String             userId;
    public int                contentId;
    public String             linkId;
    public boolean            synced;

    public StudyHistory() {

        super();
    }

    public StudyHistory(int orgKeyId, String userId, int contentId, String linkId, boolean synced) {

        super(orgKeyId);
        this.userId = userId;
        this.contentId = contentId;
        this.linkId = linkId;
        this.synced = synced;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{userId:").append(userId).append(", contentId:").append(contentId)
                .append(", linkId:").append(linkId).append(", synced:").append(synced).append(", _id:")
                .append(_id).append(", orgKeyId:").append(orgKeyId).append(", timeCreated:")
                .append(timeCreated).append("}");
        return builder.toString();
    }

}
