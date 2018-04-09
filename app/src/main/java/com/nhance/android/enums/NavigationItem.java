package com.nhance.android.enums;

import com.nhance.android.R;

public enum NavigationItem {
    ACTIVITY("Activity", R.drawable.navigation_activity),
    LIBRARY("Courses", R.drawable.course),
    LIBRARY_CONTENT("", 0),
    ANALYTICS("Analytics", R.drawable.analytics_icon),
    ANALYTICS_COURSE("Analytics", 0),
    RECENTACTIVITY("Recent Activity", R.drawable.recentactivity_icon),
    PROFILE("Profile", R.drawable.profile),
    NOTIFICATIONS("Notifications", R.drawable.notification_icon),
    DOUBTS("Doubts", R.drawable.doubts_icon),
    MESSAGE("Message", R.drawable.messages),
    CHALLENGEARENA("Challenge Arena", R.drawable.challenges),
    LEADERBOARD("Leaderboard", R.drawable.leaderboard);


    public String displayName;
    public int    icon_res_id;

    private NavigationItem() {

    }

    private NavigationItem(String displayName, int icon_res_id) {



        this.displayName = displayName;
        this.icon_res_id = icon_res_id;
    }

    public static NavigationItem valueOfKey(String value) {

        NavigationItem item = LIBRARY;
        try {
            item = valueOf(value.toUpperCase());
        } catch (Exception e) {}
        return item;
    }
}
