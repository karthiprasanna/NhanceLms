package com.nhance.android.enums;

public enum FollowType {
    FOLLOWING, FOLLOWER, BOTH_WAYS, NONE, OWNER, YOU;

    public static FollowType valueOfKey(String key) {

        FollowType followType = NONE;
        try {
            followType = valueOf(key.trim().toUpperCase());
        } catch (Exception e) {}
        return followType;
    }
}
