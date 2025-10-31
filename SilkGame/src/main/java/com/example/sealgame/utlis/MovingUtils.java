package com.example.sealgame.utlis;

import com.example.sealgame.enums.WallType;
import com.example.sealgame.model.GamingMap;

public class MovingUtils {


    public static boolean isValidMove(int x, int y,WallType[][] grid) {
        // 检查边界
        if (x < 0 || x >= GamingMap.GRID_SIZE || y < 0 || y >= GamingMap.GRID_SIZE) {
            return false;
        }

        // 检查是否有墙壁
        return grid[y][x] == WallType.NONE;
    }




}
