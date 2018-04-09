package com.nhance.android.pojos.slpmodules;

import org.json.JSONObject;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.db.models.entity.Content;
import com.nhance.android.enums.ModuleEntryCompletionRuleType;
import com.nhance.android.pojos.SrcEntity;
import com.nhance.android.utils.JSONAware;
import com.nhance.android.utils.JSONUtils;

public class ModuleEntryInfo implements JSONAware {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public String                    name;
    public SrcEntity                 entity;        // this will be null in case to module topic
    public boolean                   consumed;      // add this info at run time
    public Content                   info;          // if the module library is not fetched then it
                                                     // will be
                                                     // null

    public ModuleEntryCompletionRule completionRule;
    public boolean                   downloadable;

    @Override
    public void fromJSON(JSONObject json) {

        name = JSONUtils.getString(json, ConstantGlobal.NAME);
        JSONObject entityJSON = JSONUtils.getJSONObject(json, "entity");
        if (entityJSON != null && entityJSON.length() > 0) {
            entity = new SrcEntity();
            entity.fromJSON(entityJSON);
        }

        ModuleEntryCompletionRuleType moduleEntryCompletionRuleType = ModuleEntryCompletionRuleType
                .valueOfKey(JSONUtils.getString(JSONUtils.getJSONObject(json, "completionRule"),
                        ConstantGlobal.TYPE));
        completionRule = new ModuleEntryCompletionRule();
        completionRule.type = moduleEntryCompletionRuleType;
        // completed and info value will be populated from local db
    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

}
