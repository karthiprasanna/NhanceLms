package com.nhance.android.pojos;

import java.io.Serializable;

import org.json.JSONObject;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.enums.EntityType;
import com.nhance.android.utils.JSONAware;
import com.nhance.android.utils.JSONUtils;

public class SrcEntity implements JSONAware, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public EntityType         type;
    public String             id;

    public SrcEntity() {

    }

    public SrcEntity(EntityType type, String id) {

        this.type = type;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {

        if (null == o || !(o instanceof SrcEntity)) {
            return false;
        }
        SrcEntity e = (SrcEntity) o;
        return type != null && type == e.type && id != null && id.equals(e.id);
    }

    @Override
    public int hashCode() {

        return type == null ? 0 : (type.name() + ":" + id).hashCode();
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{type:").append(type).append(", id:").append(id).append("}");
        return builder.toString();
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

    @Override
    public void fromJSON(JSONObject json) {

        id = JSONUtils.getString(json, ConstantGlobal.ID);
        type = EntityType.valueOfKey(JSONUtils.getString(json, ConstantGlobal.TYPE));
    }
}
