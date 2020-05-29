package com.talanlabs.sample01.core;

public enum ErrorCode {

    DATABASE_ERROR(1);

    private int code;

    ErrorCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
