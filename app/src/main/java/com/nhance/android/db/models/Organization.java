package com.nhance.android.db.models;

import com.nhance.android.db.models.entity.AbstractEntity;

public class Organization extends AbstractEntity {

    /**
     * 
     */
    private static final long serialVersionUID = -4786763506806726830L;
    public String             fullName;
    public String             orgId;
    public String             thumb;
    public String             cmdsUrl;
    public String             slug;
    public String             desc;
    public String             website;
    public String             contactNo;
    public boolean            autoLogin;
    public String             authType;
    public byte[]             key;                                     // public key of the
                                                                        // organization

    public Organization() {

        this(null, null);
    }

    public Organization(String name, String fullName) {

        super(name);
        this.fullName = fullName;
    }

    public Organization(String name, String fullName, String orgId, String thumb, String desc,
            String cmdsUrl, String slug, String authType) {

        super(name);
        this.fullName = fullName;
        this.orgId = orgId;
        this.thumb = thumb;
        this.cmdsUrl = cmdsUrl;
        this.desc = desc;
        this.slug = slug;
        this.authType = authType;

    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{fullName:").append(fullName).append(", orgId:").append(orgId)
                .append(", thumb:").append(thumb).append(", cmdsUrl:").append(cmdsUrl)
                .append(", slug:").append(slug).append(", desc:").append(desc).append(", website:")
                .append(website).append(", contactNo:").append(contactNo).append(", autoLogin:")
                .append(autoLogin).append(", authType:").append(authType).append(", name:")
                .append(name).append(", _id:").append(_id).append(", orgKeyId:").append(orgKeyId)
                .append(", timeCreated:").append(timeCreated).append("}");
        return builder.toString();
    }
}
