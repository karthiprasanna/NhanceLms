package com.nhance.android.pojos;

import org.json.JSONObject;

import com.nhance.android.constants.ConstantGlobal;
import com.nhance.android.enums.MemberProfile;
import com.nhance.android.utils.JSONAware;
import com.nhance.android.utils.JSONUtils;

public class OrgMemberInfo implements JSONAware {

    /**
     * 
     */
    private static final long   serialVersionUID = 1L;
    public String               id;
    public String               userId;
    public String               memberId;
    public String               orgId;
    public String               gender;
    public String               dob;
    public MemberProfile        profile;
    public String               firstName;
    public String               lastName;
    public String               thumbnail;
    public String               email;
    public OrgMemberMappingInfo mappings;

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{id:").append(id).append(", userId:").append(userId).append(", memberId:")
                .append(memberId).append(", orgId:").append(orgId).append(", gender:")
                .append(gender).append(", dob:").append(dob).append(", profile:").append(profile)
                .append(", firstName:").append(firstName).append(", lastName:").append(lastName)
                .append(", thumbnail:").append(thumbnail).append(", email:").append(email)
                .append(", mappings:").append(mappings).append("}");
        return builder.toString();
    }

    @Override
    public void fromJSON(JSONObject json) {

        if (json == null) {
            return;
        }
        id = JSONUtils.getString(json, ConstantGlobal.ID);
        userId = JSONUtils.getString(json, ConstantGlobal.USER_ID);
        memberId = JSONUtils.getString(json, ConstantGlobal.MEMBER_ID);
        orgId = JSONUtils.getString(json, ConstantGlobal.ORG_ID);
        gender = JSONUtils.getString(json, "gender");

        dob = JSONUtils.getString(json, "dob");
        profile = MemberProfile.valueOf(JSONUtils.getString(json, "profile"));
        firstName = JSONUtils.getString(json, ConstantGlobal.FIRST_NAME);
        lastName = JSONUtils.getString(json, ConstantGlobal.LAST_NAME);
        thumbnail = JSONUtils.getString(json, ConstantGlobal.THUMBNAIL);
        email = JSONUtils.getString(json, "email");
        mappings = new OrgMemberMappingInfo();
        mappings.fromJSON(JSONUtils.getJSONObject(json, "mappings"));

    }

    @Override
    public JSONObject toJSON() {

        return null;
    }

    public boolean _isSectionMappingPresent(String sectionId) {

        return mappings != null && mappings._getSectionIds().contains(sectionId);
    }

}
