package com.nhance.android.db.models.entity;

public abstract class AbstractVEntity extends AbstractEntity {
    /**
     * 
     */
    private static final long serialVersionUID = 7366998048756405815L;
    public String by;
    public AbstractVEntity(String name) {
        super(name);
    }
}
