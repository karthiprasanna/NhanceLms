package com.nhance.android.enums;

public enum TestResultVisibility {
    VISIBLE, HIDDEN;

    public static TestResultVisibility valueOfKey(String key) {

        TestResultVisibility resultVisibility = VISIBLE;
        try {
            resultVisibility = valueOf(key.trim().toUpperCase());
        } catch (Throwable e) {}
        return resultVisibility;
    }
}
