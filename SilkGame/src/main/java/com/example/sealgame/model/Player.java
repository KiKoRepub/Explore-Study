package com.example.sealgame.model;

import com.badlogic.gdx.graphics.Color;
import lombok.Data;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * 玩家类
 * 用圆圈+一格身体表示，可以在游戏页面中移动
 */
@Data
public class Player {
    
    // 玩家在网格中的位置 (支持小数位置)
    private double gridX;
    private double gridY;
    
    // 玩家的方向 (0:上, 1:右, 2:下, 3:左)
    private int direction;
    
    // 玩家移动速度（每帧移动的距离）
    private double speed = 0.1;
    
    // 玩家拥有的线段
    private Silk silk;

    // 已射出的线段队列
    private Deque<ShootedSilk> silkQueue;
    
    // 玩家颜色 (LibGDX Color)
    private Color color;
    
    // 玩家名称
    private String name;

    // 当前是否在沿射线移动
    private boolean isMovingAlongSilk = false;
    
    // 当前沿射线移动的目标
    private ShootedSilk currentMovingTarget;
    private int alongSilkFront;
    
    /**
     * 构造函数
     */
    public Player(double gridX, double gridY, String name) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.name = name;
        this.direction = 0; // 默认朝上
        this.color = Color.BLUE;
        this.silk = new Silk(10); // 初始线段长度为10
        this.silkQueue = new ArrayDeque<>();
    }
    
    /**
     * 移动玩家（支持小数移动）
     */
    public void move(double dx, double dy) {
        this.gridX += dx;
        this.gridY += dy;
        
        // 根据移动方向更新朝向
        if (dx > 0) direction = 1; // 右
        else if (dx < 0) direction = 3; // 左
        else if (dy > 0) direction = 2; // 下
        else if (dy < 0) direction = 0; // 上
    }
    
    /**
     * 设置玩家位置
     */
    public void setPosition(double gridX, double gridY) {
        this.gridX = gridX;
        this.gridY = gridY;
    }
    
    /**
     * 射出线段
     */
    public boolean shootSeal(double targetX, double targetY) {
        if (silk == null || silk.getRemainingLength() <= 0) {
            return false;
        }
        
        // 计算射出距离
        int distance = (int) Math.ceil(Math.abs(targetX - gridX) + Math.abs(targetY - gridY));
        
        // 使用线段
        if (silk.useSilk(distance)) {
            // 添加到队列
            ShootedSilk shootedSilk = new ShootedSilk(targetX, targetY, distance, this);
            silkQueue.offer(shootedSilk);
            return true;
        }
        return false;
    }
    
    /**
     * 回收最早射出的线段
     */
    public boolean recoverOldestSilk() {
        if (silkQueue.isEmpty()) {
            return false;
        }
        
        ShootedSilk oldest = silkQueue.poll();
        silk.recoverSilk(oldest.getLength());
        return true;
    }


}
