package org.dee.enums;

public enum McpConnectionEnum {

    FORBID(1, "禁用"),
    FAULT(2, "异常"),
    CONNECTED(2, "连接成功"),
    DISCONNECTED(3, "连接失败");

    public int code;

    public String description;

    McpConnectionEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }


}
