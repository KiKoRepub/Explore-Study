package com.example.sealgame.model;

import com.badlogic.gdx.graphics.Color;
import com.example.sealgame.enums.SilkColorEnum;
import lombok.Data;

/**
 * 丝线
 * 线段类 (Silk)
 * 被玩家拥有，初始长度为10，颜色为红色
 */
@Data
public class Silk {
    
    // 线段的总长度
    private int totalLength;
    
    // 已使用的长度
    private int usedLength;
    
    // 线段颜色
    private Color color;
    
    // 线段宽度（像素）
    private int width;
    
    /**
     * 构造函数
     * @param initialLength 初始线段长度
     */
    public Silk(int initialLength) {
        this.totalLength = initialLength;
        this.usedLength = 0;
        this.color = SilkColorEnum.RED.color; // 默认红色
        this.width = 3; // 默认宽度3像素
    }
    
    /**
     * 使用线段
     * @param length 要使用的长度
     * @return 是否成功使用
     */
    public boolean useSilk(int length) {
        if (getRemainingLength() >= length) {
            usedLength += length;
            return true;
        }
        return false;
    }

    public boolean recoverSilk(int length){
        if (usedLength >= length) {
            usedLength -= length;
            return true;
        }
        return false;
    }
    
    /**
     * 获取剩余长度
     */
    public int getRemainingLength() {
        return totalLength - usedLength;
    }
    
    /**
     * 重置线段（恢复到初始状态）
     */
    public void reset() {
        this.usedLength = 0;
    }
    
    /**
     * 增加总长度
     */
    public void addLength(int additionalLength) {
        this.totalLength += additionalLength;
    }
    
    // Getters and Setters
    
    public int getTotalLength() {
        return totalLength;
    }
    
    public void setTotalLength(int totalLength) {
        this.totalLength = totalLength;
    }
    
    public int getUsedLength() {
        return usedLength;
    }
    
    public void setUsedLength(int usedLength) {
        this.usedLength = usedLength;
    }
    
    public Color getColor() {
        return color;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    public int getWidth() {
        return width;
    }
    
    public void setWidth(int width) {
        this.width = width;
    }
}
