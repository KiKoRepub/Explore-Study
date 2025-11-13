package org.dee.enums;

public enum ToolTypeEnum {

    STANDARD,
    MCP,
    USER;

    public final String value;

    ToolTypeEnum() {
        this.value = this.name();
    }
}
