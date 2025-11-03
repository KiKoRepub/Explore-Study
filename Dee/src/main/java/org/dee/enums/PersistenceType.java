package org.dee.enums;

import lombok.Getter;

/**
 * 持久化类型枚举
 * 用于区分自动持久化和手动持久化
 */
@Getter
public enum PersistenceType {
    
    /**
     * 自动持久化
     * 通过 Redis 键过期事件自动触发
     */
    AUTO("auto", "自动持久化"),
    
    /**
     * 手动持久化
     * 用户主动调用持久化接口
     */
    MANUAL("manual", "手动持久化");

    /**
     * 类型代码
     */
    private final String code;

    /**
     * 类型描述
     */
    private final String description;

    PersistenceType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     */
    public static PersistenceType fromCode(String code) {
        for (PersistenceType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}
