package com.example.sealgame.utlis;

import com.example.sealgame.enums.WallType;

/**
 * 游戏地图工具类
 * 用于初始化和管理游戏地图
 */
public class GamingMapUtils {

    public static final int CELL_SIZE = 30;
    public static final int GRID_SIZE = 20;



    public static WallType[][] initializeGrid() {
        WallType [] [] grid = new WallType[GRID_SIZE][GRID_SIZE];

        // 初始化所有格子为空地
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = WallType.NONE;
            }
        }

        // 添加边界墙壁
        for (int i = 0; i < GRID_SIZE; i++) {
            grid[0][i] = WallType.BOUNDARY_WALL; // 上边界
            grid[GRID_SIZE - 1][i] = WallType.BOUNDARY_WALL; // 下边界
            grid[i][0] = WallType.BOUNDARY_WALL; // 左边界
            grid[i][GRID_SIZE - 1] = WallType.BOUNDARY_WALL; // 右边界
        }

        // 添加一些障碍物
        grid[5][5] = WallType.NORMAL_WALL;
        grid[5][6] = WallType.NORMAL_WALL;
        grid[5][7] = WallType.NORMAL_WALL;

        grid[10][10] = WallType.SOLID_WALL;
        grid[10][11] = WallType.SOLID_WALL;
        grid[11][10] = WallType.SOLID_WALL;
        grid[11][11] = WallType.SOLID_WALL;

        grid[15][8] = WallType.BREAKABLE_WALL;
        grid[15][9] = WallType.BREAKABLE_WALL;
        grid[15][10] = WallType.BREAKABLE_WALL;

        return grid;
    }
}
