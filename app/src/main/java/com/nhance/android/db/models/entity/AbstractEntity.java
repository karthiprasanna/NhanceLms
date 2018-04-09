package com.nhance.android.db.models.entity;

import com.nhance.android.db.models.AbstractDataModel;
import com.nhance.android.pojos.IListResponseObj;

public abstract class AbstractEntity extends AbstractDataModel implements IListResponseObj {

    /**
     * 
     */
    private static final long serialVersionUID = 7146691402492558197L;
    public String             name;

    public AbstractEntity(String name) {

        super();
        this.name = name;
    }

    // public abstract AbstractEntityView getEntityView(Context context);
}
