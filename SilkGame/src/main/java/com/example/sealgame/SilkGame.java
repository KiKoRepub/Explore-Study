package com.example.sealgame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.example.sealgame.screen.GameScreen;

/**
 * LibGDX 主游戏类
 * 管理游戏屏幕和全局资源
 */
public class SilkGame extends Game {
    
    // 全局渲染器
    public SpriteBatch batch;
    public ShapeRenderer shapeRenderer;
    public BitmapFont font;
    
    @Override
    public void create() {
        // 初始化渲染器
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        
        // 设置字体
        font.getData().setScale(1.2f);
        
        Gdx.app.log("SilkGame", "Game initialized");
        
        // 切换到游戏屏幕
        setScreen(new GameScreen(this));
    }
    
    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (font != null) font.dispose();
        
        // 释放当前屏幕
        if (getScreen() != null) {
            getScreen().dispose();
        }
    }
}
