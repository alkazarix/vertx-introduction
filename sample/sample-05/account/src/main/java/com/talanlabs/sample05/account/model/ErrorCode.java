package com.talanlabs.sample05.account.model;

public enum ErrorCode {
    DATABASE_ERROR(1),
    ACCOUNT_NOT_FOUND(3);

    private int code;

    ErrorCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
