package com.nhance.android.db.models.entity;

import com.nhance.android.enums.ContextType;
import com.nhance.android.enums.EncryptionLevel;

public abstract class AbstractEncContentEntity extends AbstractContentEntity {

    public String passPhrase;
    public String encLevel;

    public AbstractEncContentEntity(String name) {
        super(name);
    }

    public AbstractEncContentEntity(String name, String targetName, String targetId,
            String orgId, String progId, String userId, String tags,
            ContextType contextType, String passPhrase, EncryptionLevel encLevel) {
        super(name, targetName, targetId, orgId, progId, userId, tags, contextType);
        this.passPhrase = passPhrase;
        this.encLevel = encLevel != null ? encLevel.name() : null;
    }

    /**
     * 
     */
    private static final long serialVersionUID = -5941210097486358248L;

}
