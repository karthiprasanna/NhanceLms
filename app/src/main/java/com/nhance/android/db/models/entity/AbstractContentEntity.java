package com.nhance.android.db.models.entity;

import com.nhance.android.enums.ContextType;

public abstract class AbstractContentEntity extends AbstractVEntity {

    /**
     * 
     */
    private static final long serialVersionUID = -2089504918844968724L;
    public String             targetName;
    public String             targetId;
    public String             progId;
    public String             orgId;
    public String             userId;
    public String             tags;                                     // SQLDBUtil.SEPARATOR
                                                                        // seperated values
    public String             contextType;

    public AbstractContentEntity(String name) {

        super(name);
    }

    public AbstractContentEntity(String name, String targetName, String targetId, String orgId,
            String progId, String userId, String tags, ContextType contextType) {

        super(name);
        this.targetName = targetName;
        this.targetId = targetId;
        this.orgId = orgId;
        this.progId = progId;
        this.userId = userId;
        this.tags = tags;
        this.contextType = contextType == null ? null : contextType.name();

    }

}
