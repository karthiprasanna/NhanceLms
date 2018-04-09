package com.nhance.android.ChallengeArena.model;

/**
 * Created by administrator on 9/5/17.
 */

public class LeaderInfo {
    public String rank;
    public long timeTaken;
    public String thumbnail;
    public String firstName;
    public String lastName;
    public String id;

    @Override
    public String toString() {
        return "LeaderInfo{" +
                "rank='" + rank + '\'' +
                ", timeTaken=" + timeTaken +
                ", thumbnail='" + thumbnail + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
