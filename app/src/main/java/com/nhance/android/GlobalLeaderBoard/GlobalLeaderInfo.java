package com.nhance.android.GlobalLeaderBoard;

/**
 * Created by administrator on 9/5/17.
 */

public class GlobalLeaderInfo {
    public String rank;
    public String points;
    public String thumbnail;
    public String firstName;

    @Override
    public String toString() {
        return "GlobalLeaderInfo{" +
                "rank='" + rank + '\'' +
                ", points='" + points + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }

    public  String lastName;

}
