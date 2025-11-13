package com.example.sealgame.enums;

import com.badlogic.gdx.graphics.Color;

/**
 * 墙壁类型枚举
 * 不同类型的墙壁用不同颜色区分
 */
public enum WallType {
    NONE(0, new Color(0, 0, 0, 0), "空地"),
    NORMAL(0, Color.GRAY, "普通"),
    BORDER(1, Color.DARK_GRAY, "水"),
    NORMAL_WALL(1, Color.GRAY, "普通墙壁"),
    SOLID_WALL(2, Color.DARK_GRAY, "坚固墙壁"),
    BREAKABLE_WALL(3, new Color(0.6f, 0.4f, 0.2f, 1), "可破坏墙壁"),
    BOUNDARY_WALL(4, Color.BLACK, "边界墙壁");
    
    private final int value;
    private final Color color;
    private final String description;
    
    WallType(int value, Color color, String description) {
        this.value = value;
        this.color = color;
        this.description = description;
    }
    
    public int getValue() {
        return value;
    }
    
    public Color getColor() {
        return color;
    }
    
    public String getDescription() {
        return description;
    }
}
