package com.example.sealgame;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/**
 * LibGDX 桌面启动器
 * 程序入口点
 */
public class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        
        // 窗口配置
        config.setTitle("Silk Seal Game - LibGDX Edition");
        config.setWindowedMode(800, 650);
        config.setResizable(false);
        
        // FPS 配置
        config.setForegroundFPS(60);
        config.setIdleFPS(30);
        
        // 启动应用
        new Lwjgl3Application(new SilkGame(), config);
    }
}
