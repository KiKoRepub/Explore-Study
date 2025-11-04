package org.dee.enums;


public enum ErrorCodeEnum {

    SUCCESS(200),
    AUTH_ERROR(401),
    URL_ERROR(402),
    PARAM_ERROR(996),
    TOKEN_ERROR(407),
    NO_FUNC_ERROR(406),
    SENTINEL_ERROR(429),
    SERVICE_ERROR(503),
    GATEWAY_TIMEOUT(504),
    UNKNOWN_ERROR(997),
    NO_USER(998),
    FAIL(999),


    ;
    private int code;

    ErrorCodeEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}