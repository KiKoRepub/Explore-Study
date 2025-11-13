package com.example.sealgame.utlis;

import com.example.sealgame.enums.WallType;

/**
 * 墙边缘移动工具类
 * 处理玩家只能在墙的边缘移动的逻辑
 */
public class WallEdgeUtils {
    
    private static final int GRID_SIZE = GamingMapUtils.GRID_SIZE;
    
    /**
     * 检查指定位置是否在墙的边缘
     * @param x X坐标
     * @param y Y坐标
     * @param grid 网格地图
     * @return 是否在墙边缘
     */
    public static boolean isOnWallEdge(double x, double y, WallType[][] grid) {
        int gridX = (int) Math.floor(x);
        int gridY = (int) Math.floor(y);
        
        // 检查边界
        if (gridX < 0 || gridX >= GRID_SIZE || 
            gridY < 0 || gridY >= GRID_SIZE) {
            return false;
        }
        
        // 当前位置必须是空地
        if (grid[gridY][gridX] != WallType.NONE) {
            return false;
        }
        
        // 检查四周是否有墙壁
        return hasAdjacentWall(gridX, gridY, grid);
    }
    
    /**
     * 检查指定位置四周是否有墙壁
     */
    private static boolean hasAdjacentWall(int gridX, int gridY, WallType[][] grid) {
        // 检查上下左右四个方向
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        
        for (int[] dir : directions) {
            int newX = gridX + dir[0];
            int newY = gridY + dir[1];
            
            if (newX >= 0 && newX < GRID_SIZE && 
                newY >= 0 && newY < GRID_SIZE) {
                if (grid[newY][newX] != WallType.NONE) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * 获取玩家可以移动到的最近墙边缘位置
     * @param x 当前X坐标
     * @param y 当前Y坐标
     * @param dx X方向移动
     * @param dy Y方向移动
     * @param grid 网格地图
     * @return 新的坐标 [newX, newY]
     */
    public static double[] getValidEdgePosition(double x, double y, double dx, double dy, WallType[][] grid) {
        double newX = x + dx;
        double newY = y + dy;
        
        // 检查新位置是否有效
        int gridX = (int) Math.floor(newX);
        int gridY = (int) Math.floor(newY);
        
        // 边界检查
        if (gridX < 0 || gridX >= GRID_SIZE || 
            gridY < 0 || gridY >= GRID_SIZE) {
            return new double[]{x, y}; // 保持原位置
        }
        
        // 检查是否撞墙
        if (grid[gridY][gridX] != WallType.NONE) {
            return new double[]{x, y}; // 保持原位置
        }
        
        // 检查是否在墙边缘
        if (isOnWallEdge(newX, newY, grid)) {
            return new double[]{newX, newY};
        }
        
        // 如果不在墙边缘，尝试调整到最近的墙边缘
        return adjustToNearestEdge(newX, newY, grid);
    }
    
    /**
     * 调整到最近的墙边缘
     */
    private static double[] adjustToNearestEdge(double x, double y, WallType[][] grid) {
        int gridX = (int) Math.floor(x);
        int gridY = (int) Math.floor(y);
        
        // 检查四周，找到最近的墙
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        
        for (int[] dir : directions) {
            int checkX = gridX + dir[0];
            int checkY = gridY + dir[1];
            
            if (checkX >= 0 && checkX < GRID_SIZE && 
                checkY >= 0 && checkY < GRID_SIZE) {
                if (grid[checkY][checkX] != WallType.NONE) {
                    // 找到墙，返回当前位置（已经在墙边缘）
                    return new double[]{x, y};
                }
            }
        }
        
        // 如果四周都没有墙，保持原位置
        return new double[]{x, y};
    }
    
    /**
     * 检查玩家是否可以在墙表面移动
     * @param x 当前X坐标
     * @param y 当前Y坐标
     * @param dx X方向移动
     * @param dy Y方向移动
     * @param grid 网格地图
     * @return 是否可以移动
     */
    public static boolean canMoveOnWallSurface(double x, double y, double dx, double dy, WallType[][] grid) {
        double newX = x + dx;
        double newY = y + dy;
        
        int gridX = (int) Math.floor(newX);
        int gridY = (int) Math.floor(newY);
        
        // 边界检查
        if (gridX < 0 || gridX >= GRID_SIZE || 
            gridY < 0 || gridY >= GRID_SIZE) {
            return false;
        }
        
        // 不能移动到墙内
        if (grid[gridY][gridX] != WallType.NONE) {
            return false;
        }
        
        // 新位置必须也在墙边缘
        return isOnWallEdge(newX, newY, grid);
    }
}
