package com.example.sealgame.enums;

import com.badlogic.gdx.graphics.Color;

public enum SilkColorEnum {
    RED(1,Color.RED),
    BLUE(2,Color.BLUE);

    final int value;
    public final Color color;

    SilkColorEnum(int value, Color color) {
        this.value = value;
        this.color=color;
    }
}
