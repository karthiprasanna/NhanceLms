package com.nhance.android.db.models.entity;

import com.nhance.android.db.models.AbstractDataModel;

public class User extends AbstractDataModel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public String             firstName;
    public String             lastName;
    public String             username;
    public String             userId;
    public String             password;
    public String             thumb;
    public String             lastLogin;
    public boolean            autoLogin;
    public byte[]             key;

    public User() {

        super();
    }

    public User(int orgKeyId, String firstName, String lastName, String username, String userId,
            String password, String thumb, String lastLogin) {

        super();
        this.orgKeyId = orgKeyId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.userId = userId;
        this.password = password;
        this.thumb = thumb;
        this.lastLogin = lastLogin;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{firstName:").append(firstName).append(", lastName:").append(lastName)
                .append(", username:").append(username).append(", userId:").append(userId)
                .append(", thumb:").append(thumb).append(", lastLogin:").append(lastLogin)
                .append(", _id:").append(_id).append(", orgKeyId:").append(orgKeyId)
                .append(", timeCreated:").append(timeCreated).append("}");
        return builder.toString();
    }

}
