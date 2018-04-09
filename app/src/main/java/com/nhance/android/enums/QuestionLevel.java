package com.nhance.android.enums;

import java.util.Locale;

import com.nhance.android.R;

public enum QuestionLevel {
    TOUGH(R.color.red), EASY(R.color.green), MODERATE(R.color.blue);

    int colorIndicator;

    private QuestionLevel(int colorIndicator) {

        this.colorIndicator = colorIndicator;
    }

    public static QuestionLevel valueOfKey(String key) {

        QuestionLevel level = EASY;
        try {
            level = valueOf(key.trim().toUpperCase(Locale.getDefault()));
        } catch (Exception e) {}
        return level;

    }

    public int getColorIndicator() {

        return colorIndicator;
    }

}
