package com.nhance.android.enums;

public enum SectionAccessScope {

    CLOSED, OPEN;

    public static SectionAccessScope valueOfKey(String key) {

        SectionAccessScope scope = CLOSED;
        try {
            scope = SectionAccessScope.valueOf(key.trim().toUpperCase());
        } catch (Exception e) {
            // swallow
        }
        return scope;
    }
}
