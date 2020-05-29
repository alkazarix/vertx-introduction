package com.talanlabs.sample04.core.models;

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
