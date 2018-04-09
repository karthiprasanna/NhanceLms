package com.nhance.android.enums;

public enum SectionRevenueModel {
    FREE, PAID;

    public static SectionRevenueModel valueOfKey(String key) {

        SectionRevenueModel revenueModel = FREE;
        try {
            revenueModel = SectionRevenueModel.valueOf(key.trim().toUpperCase());
        } catch (Exception e) {
            // swallow
        }
        return revenueModel;
    }
}
