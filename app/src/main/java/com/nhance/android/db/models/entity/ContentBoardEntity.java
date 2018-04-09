package com.nhance.android.db.models.entity;

import com.nhance.android.db.models.AbstractDataModel;

public abstract class ContentBoardEntity extends AbstractDataModel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public String             userId;               // data for userId
    public String             ownerId;
    public String             ownerName;
    // common content details
    public String             lastUpdated;
    public boolean            downloaded;
    public boolean            starred;
    public String             lastViewed;
    public String             brdIds;               // local _ids for boards {for multiple courses
                                                     // keep
                                                     // SQLDBUtils.SEPRATOR separated values}
    // NOTE: all courseIds, topic ids and subtopic ids will be saved in brdIds field and there will
    // a mapping of parent child relationship in a separate table (BOARD)

    public String             tags;                 // SQLDBUtil.SEPARATOR separated values
    public String             targetIds;            // target ExamIds
    public String             targetNames;          // target ExamNames
    

    public ContentBoardEntity() {

        super();
    }

    public ContentBoardEntity(String timeCreated, int orgKeyId) {

        super(timeCreated, orgKeyId);
        this.lastViewed = String.valueOf(0);
    }

    public ContentBoardEntity(String timeCreated, int orgKeyId, String lastUpdated, String brdIds,
            String tags) {

        super(timeCreated, orgKeyId);
        this.lastViewed = String.valueOf(0);
        this.lastUpdated = lastUpdated;
        this.brdIds = brdIds;
        this.tags = tags;
        this.downloaded = false;
    }

    public abstract boolean equalsIgnoreCase(String attempted);
}
