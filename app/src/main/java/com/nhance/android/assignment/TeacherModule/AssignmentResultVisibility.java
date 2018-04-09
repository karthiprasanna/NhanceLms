package com.nhance.android.assignment.TeacherModule;

/**
 * Created by karthi on 1/3/17.
 */
public enum AssignmentResultVisibility {
    VISIBLE, HIDDEN;

    public static AssignmentResultVisibility valueOfKey(String key) {

        AssignmentResultVisibility resultVisibility = VISIBLE;
        try {
            resultVisibility = valueOf(key.trim().toUpperCase());
        } catch (Throwable e) {}
        return resultVisibility;
    }
}
