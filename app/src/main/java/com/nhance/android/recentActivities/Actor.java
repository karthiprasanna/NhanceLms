package com.nhance.android.recentActivities;

import org.json.JSONObject;

/**
 * Created by administrator on 4/4/17.
 */

public class Actor {
    public String firstName;
    public String lastName;
    public String id;
    public String type;
    public String thumbnail;
    public String profile;
    public String message;
    public String dateTime;
    public int upVoteCount;
    public int replyCount;
    public boolean upVoted;
    public JSONObject srcObject;

    @Override
    public String toString() {
        return "Actor{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", profile='" + profile + '\'' +
                ", message='" + message + '\'' +
                ", dateTime='" + dateTime + '\'' +
                ", upVoteCount='" + upVoteCount + '\'' +
                ", replyCount='" + replyCount + '\'' +
                ", srcObject=" + srcObject +
                '}';
    }
}
