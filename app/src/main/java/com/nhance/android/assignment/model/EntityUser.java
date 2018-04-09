package com.nhance.android.assignment.model;



/**
 * Created by administrator on 2/13/17.
 */

public class EntityUser {

    public  int id;

    public String type;

    public String targetUserId;

    @Override
    public String toString() {
        return "EntityUser{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", targetUserId='" + targetUserId + '\'' +
                '}';
    }

}
