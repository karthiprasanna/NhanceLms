package com.nhance.android.db.models;

import org.json.JSONObject;

public class UserOrgProfile extends AbstractDataModel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public String             userId;
    public String             orgId;
    public JSONObject         orgProfile;

    public UserOrgProfile() {

        super();
    }

    public UserOrgProfile(int orgKeyId, String userId, String orgId, JSONObject orgProfile) {

        super();
        this.orgKeyId = orgKeyId;
        this.userId = userId;
        this.orgId = orgId;
        this.orgProfile = orgProfile;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{userId:").append(userId).append(", orgProfile:").append(orgProfile)
                .append(", _id:").append(_id).append(", timeCreated:").append(timeCreated)
                .append("}");
        return builder.toString();
    }

}
