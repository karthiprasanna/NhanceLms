package com.nhance.android.enums;

import com.nhance.android.R;
public enum ErrorCode {
    UNKNOWN, FILE_NOT_FOUND(R.string.error_file_not_present), INVALID_CODE(
            R.string.error_access_code_invalid), ITEM_ALREADY_VERIFIED_FOR_DIFFERENT_USER(
            R.string.error_access_code_invalid), ITEM_ALREADY_VERIFIED_WITH_DIFFERENT_DEVICE(
            R.string.error_access_code_already_used_in_different_divice), INVALID_ITEM(
            R.string.error_access_code_invalid);

    private int errorMessageResource;

    private ErrorCode() {

        this(-1);
    }

    private ErrorCode(int errorMessageResource) {

        this.errorMessageResource = errorMessageResource;
    }

    public int getErrorMessageResource() {

        return errorMessageResource;
    }

    public static ErrorCode valueOfKey(String key) {

        ErrorCode errorCode = UNKNOWN;
        try {
            errorCode = valueOf(key.trim().toUpperCase());
        } catch (Throwable e) {}
        return errorCode;
    }
}
