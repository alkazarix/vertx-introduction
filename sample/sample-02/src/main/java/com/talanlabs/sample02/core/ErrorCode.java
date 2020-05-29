package com.talanlabs.sample02.core;

public enum ErrorCode {

    DATABASE_ERROR(1),
    UNKNOWN_METHOD(2),
    QUESTION_NOT_FOUND(3);

    private int code;

    ErrorCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
