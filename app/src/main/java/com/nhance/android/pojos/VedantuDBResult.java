package com.nhance.android.pojos;

import java.util.ArrayList;
import java.util.List;

public class VedantuDBResult<T extends IListResponseObj> {

    public List<T> entities;
    public int     totalHits;

    public VedantuDBResult(int totalHits) {

        this.totalHits = totalHits;
        this.entities = new ArrayList<T>();
    }

    public void addEntity(T entity) {

        if (entities == null) {
            entities = new ArrayList<T>();
        }
        entities.add(entity);
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{ entities:").append(entities).append(", totalHits:").append(totalHits)
                .append("}");
        return builder.toString();
    }

}
