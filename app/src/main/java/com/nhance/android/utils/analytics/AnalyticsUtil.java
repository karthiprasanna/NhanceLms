package com.nhance.android.utils.analytics;

import android.util.Log;

import com.nhance.android.R;

public class AnalyticsUtil {

    public static float getPercentage(int totalMarks, int score) {

        return totalMarks == 0 ? 0 : (score * 100) / totalMarks;
    }

    public static String getStringDisplayValue(float value) {

        int intValue = (int) value;
        String str = value > intValue && value < (intValue + 1) ? String.valueOf(value) : String
                .valueOf(intValue);
        return str;
    }

    public static String getStringDisplayValue(float value, int roundOffIndex) {

        String arg = "%." + roundOffIndex + "f";
        Log.d("TMP", arg);
        String str = String.format(arg, value);
        str = getStringDisplayValue(Float.parseFloat(str));
        return str;
    }

    public static int getPercentageColorCode(float percentage) {

        int scoredBgColor = R.color.green;
        if (percentage <= 30) {
            scoredBgColor = R.color.red;
        } else if (percentage <= 60) {
            scoredBgColor = R.color.orange;
        } else if (percentage <= 80) {
            scoredBgColor = R.color.yellow;
        }
        return scoredBgColor;
    }

    public static int getStrengthColorCode(float percentage) {

        int scoredBgColor = R.color.darkergrey;
        if (percentage <= 30) {
            scoredBgColor = R.color.red;
        }
        return scoredBgColor;
    }

    public static int getStrengthDisplayStringCode(float percentage) {

        int scoredStrengthCode = R.string.strength_average;
        if (percentage <= 30) {
            scoredStrengthCode = R.string.strength_weak;
        } else if (percentage > 70) {
            scoredStrengthCode = R.string.strength_strong;
        }
        return scoredStrengthCode;
    }
}
