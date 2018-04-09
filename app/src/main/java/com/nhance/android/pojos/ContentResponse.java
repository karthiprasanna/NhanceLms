package com.nhance.android.pojos;

import com.nhance.android.enums.ErrorCode;

public class ContentResponse {

    public String url;
    public String errorMessege;
    public ErrorCode errorCode;

    public ContentResponse(String url, ErrorCode errorCode) {
        this.url = url;
        this.errorCode = errorCode;
        this.errorMessege = this.errorCode.name();
    }

    public ContentResponse(String url, String errorMessege, ErrorCode errorCode) {
        this.url = url;
        this.errorMessege = errorMessege;
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{url:").append(url).append(", errorMessege:")
                .append(errorMessege).append(", errorCode:").append(errorCode)
                .append("}");
        return builder.toString();
    }

}
