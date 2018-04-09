package com.nhance.android.pojos.tests;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.utils.JSONAware;
import com.nhance.android.utils.JSONUtils;

public class BoardQus implements JSONAware, Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;
    public String             id;
    public String             name;
    public int                qusCount;
    public int                totalMarks;
    public List<String>       qIds;

    public BoardQus() {

        this(StringUtils.EMPTY, StringUtils.EMPTY, 0);
    }

    public BoardQus(String id, String name, int qusCount) {

        this.id = id;
        this.name = name;
        this.qusCount = qusCount;
        this.qIds = new ArrayList<String>();
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

    @Override
    public void fromJSON(JSONObject json) {

        id = JSONUtils.getString(json, ConstantGlobal.ID);
        name = JSONUtils.getString(json, ConstantGlobal.NAME);
        qusCount = JSONUtils.getInt(json, ConstantGlobal.QUS_COUNT);
        qIds = JSONUtils.getList(json, ConstantGlobal.QIDS);
    }

    @Override
    public int hashCode() {

        return ((id == null) ? 0 : id.hashCode());
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null || getClass() != obj.getClass())
            return false;
        BoardQus other = (BoardQus) obj;
        return StringUtils.equals(id, other.id);
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("BoardQus [id:").append(id).append(", name:").append(name)
                .append(", qusCount:").append(qusCount).append(", qIds:").append(qIds).append("]");
        return builder.toString();
    }

}
