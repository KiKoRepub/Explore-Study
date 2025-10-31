package com.example.sealgame.model;

import lombok.Data;

/**
 * 已射出的线段类
 * 记录线段的目标位置、长度和所属玩家
 */
@Data
public class ShootedSilk {

    // 线段目标位置
    private double xTarget;
    private double yTarget;
    
    // 线段起始位置（玩家射出时的位置）
    private double xStart;
    private double yStart;
    
    // 线段长度
    private int length;

    // 所属玩家
    private Player attachedPlayer;

    public ShootedSilk(double xTarget, double yTarget, int length, Player player) {
        this.xTarget = xTarget;
        this.yTarget = yTarget;
        this.length = length;
        this.attachedPlayer = player;
        
        // 记录射出时玩家的位置
        this.xStart = player.getGridX();
        this.yStart = player.getGridY();
    }
    
    /**
     * 检查玩家是否在线段允许的范围内
     */
    public boolean isPlayerInRange(double playerX, double playerY) {
        // 计算玩家到起点的距离
        double distanceFromStart = Math.abs(playerX - xStart) + Math.abs(playerY - yStart);
        
        // 玩家不能超过线段长度
        return distanceFromStart <= length;
    }
    
    /**
     * 获取线段的方向向量（归一化）
     */
    public double[] getDirectionVector() {
        double dx = xTarget - xStart;
        double dy = yTarget - yStart;
        double magnitude = Math.sqrt(dx * dx + dy * dy);
        
        if (magnitude == 0) {
            return new double[]{0, 0};
        }
        
        return new double[]{dx / magnitude, dy / magnitude};
    }

}
