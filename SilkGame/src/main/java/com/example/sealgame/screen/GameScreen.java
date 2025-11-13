package com.example.sealgame.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.example.sealgame.SilkGame;
import com.example.sealgame.enums.WallType;
import com.example.sealgame.model.Player;
import com.example.sealgame.model.ShootedSilk;
import com.example.sealgame.utlis.GamingMapUtils;
import com.example.sealgame.utlis.WallEdgeUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * LibGDX 游戏屏幕
 * 处理游戏逻辑、渲染和输入
 */
public class GameScreen implements Screen {
    
    private final SilkGame game;
    
    // 相机和视口
    private OrthographicCamera camera;
    private Stage stage;
    
    // 游戏数据
    private WallType[][] grid;
    private Player player;
    
    // 常量
    private static final int GRID_SIZE = 20;
    private static final int CELL_SIZE = 30;
    private static final int GAME_WIDTH = GRID_SIZE * CELL_SIZE;
    private static final int GAME_HEIGHT = GRID_SIZE * CELL_SIZE;
    
    // 输入状态
    private Set<Integer> pressedKeys = new HashSet<>();
    
    // UI 标签
    private Label playerNameLabel;
    private Label positionLabel;
    private Label silkInfoLabel;
    private Label instructionLabel;
    
    public GameScreen(SilkGame game) {
        this.game = game;
    }
    
    @Override
    public void show() {
        // 初始化相机
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 650);
        
        // 初始化 UI Stage
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);
        
        // 初始化游戏数据
        grid = GamingMapUtils.initializeGrid();
        player = new Player(1.5, 1.5, "Player1");
        
        // 创建 UI
        createUI();
        
        Gdx.app.log("GameScreen", "Game screen initialized");
    }
    
    /**
     * 创建 UI 界面
     */
    private void createUI() {
        // 创建简单的标签样式
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = game.font;
        labelStyle.fontColor = Color.WHITE;
        
        // 创建主表格
        Table table = new Table();
        table.top().right();
        table.setFillParent(true);
        table.pad(10);
        
        // 玩家信息
        playerNameLabel = new Label("Player: Player1", labelStyle);
        positionLabel = new Label("Position: (1.5, 1.5)", labelStyle);
        silkInfoLabel = new Label("Silk: 10/10", labelStyle);
        instructionLabel = new Label("A/D: Move | W: Follow Silk | S: Stop | Q: Recover", labelStyle);
        
        table.add(playerNameLabel).row();
        table.add(positionLabel).row();
        table.add(silkInfoLabel).row();
        table.add(instructionLabel).padTop(20).row();
        
        stage.addActor(table);
    }
    
    @Override
    public void render(float delta) {
        // 更新游戏逻辑
        update(delta);
        
        // 清屏
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        // 更新相机
        camera.update();
        game.shapeRenderer.setProjectionMatrix(camera.combined);
        game.batch.setProjectionMatrix(camera.combined);
        
        // 渲染游戏
        renderGame();
        
        // 渲染 UI
        stage.act(delta);
        stage.draw();
    }
    
    /**
     * 更新游戏逻辑
     */
    private void update(float delta) {
        // 处理输入
        handleInput();
        
        // 处理连续移动
        if (pressedKeys.contains(Input.Keys.A)) {
            movePlayerAlongWall(-player.getSpeed(), 0);
        }
        if (pressedKeys.contains(Input.Keys.D)) {
            movePlayerAlongWall(player.getSpeed(), 0);
        }
        
        // 处理沿线段移动
        if (player.isMovingAlongSilk() && player.getCurrentMovingTarget() != null) {
            movePlayerAlongSilk();
        }
        
        // 更新 UI
        updateUI();
    }
    
    /**
     * 处理输入
     */
    private void handleInput() {
        // 跟踪按键状态
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            pressedKeys.add(Input.Keys.A);
        } else {
            pressedKeys.remove(Input.Keys.A);
        }
        
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            pressedKeys.add(Input.Keys.D);
        } else {
            pressedKeys.remove(Input.Keys.D);
        }
        
        // W 键：开始沿线段移动
        if (Gdx.input.isKeyJustPressed(Input.Keys.W) && !player.getSilkQueue().isEmpty()) {
            ShootedSilk latestSilk = player.getSilkQueue().peekLast();
            if (latestSilk != null) {
                player.setMovingAlongSilk(true);
                player.setCurrentMovingTarget(latestSilk);
                Gdx.app.log("GameScreen", "Started moving along silk");
            }
        }
        
        // S 键：停止移动
        if (Gdx.input.isKeyJustPressed(Input.Keys.S) && player.isMovingAlongSilk()) {
            // 朝后移动

            Gdx.app.log("GameScreen", "Stopped moving");
        }
        
        // Q 键：回收线段
        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            if (player.recoverOldestSilk()) {
                Gdx.app.log("GameScreen", "Recovered silk");
            } else {
                Gdx.app.log("GameScreen", "No silk to recover");
            }
        }
        
        // 鼠标点击：射出线段
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            handleMouseClick();
        }
    }
    
    /**
     * 处理鼠标点击
     */
    private void handleMouseClick() {
        Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(touchPos);
        
        int gridX = (int) (touchPos.x / CELL_SIZE);
        int gridY = (int) (touchPos.y / CELL_SIZE);
        
        if (gridX >= 0 && gridX < GRID_SIZE && gridY >= 0 && gridY < GRID_SIZE) {
            shootSeal(gridX, gridY);
        }
    }
    
    /**
     * 射出线段
     */
    private void shootSeal(int targetX, int targetY) {
        // 检查目标是否是墙壁
        if (grid[targetY][targetX] == WallType.NONE) {
            Gdx.app.log("GameScreen", "Target is not a wall");
            return;
        }
        
        // 计算距离
        int distance = (int) Math.ceil(Math.abs(targetX - player.getGridX()) + Math.abs(targetY - player.getGridY()));
        
        // 检查线段是否足够
        if (player.getSilk().getRemainingLength() < distance) {
            Gdx.app.log("GameScreen", "Not enough silk length");
            return;
        }
        
        // 射出线段
        if (player.shootSeal(targetX, targetY)) {
            Gdx.app.log("GameScreen", "Shot silk to (" + targetX + ", " + targetY + ")");
        }
    }
    
    /**
     * 沿墙边缘移动
     */
    private void movePlayerAlongWall(double dx, double dy) {
        double newX = player.getGridX() + dx;
        double newY = player.getGridY() + dy;
        
        // 检查是否可以在墙表面移动
        if (WallEdgeUtils.canMoveOnWallSurface(player.getGridX(), player.getGridY(), dx, dy, grid)) {
            // 检查线段范围限制
            if (isWithinSilkRange(newX, newY)) {
                player.move(dx, dy);
            }
        }
    }
    
    /**
     * 沿线段移动
     */
    private void movePlayerAlongSilk() {
        ShootedSilk target = player.getCurrentMovingTarget();
        int front = player.getAlongSilkFront();
        if (target == null) {
            return;
        }
        
        double[] direction = target.getDirectionVector();
        double dx = direction[0] * player.getSpeed();
        double dy = direction[1] * player.getSpeed();
        
        double newX = player.getGridX() + dx;
        double newY = player.getGridY() + dy;
        
        // 检查是否到达目标
        double distToTarget = Math.sqrt(
            Math.pow(newX - target.getXTarget(), 2) + 
            Math.pow(newY - target.getYTarget(), 2)
        );

        if (distToTarget < player.getSpeed()) {
            player.setMovingAlongSilk(false);
            player.setCurrentMovingTarget(null);
            Gdx.app.log("GameScreen", "Reached target");
            return;
        }
        
        // 检查碰撞
        int gridX = (int) Math.floor(newX);
        int gridY = (int) Math.floor(newY);
        
        if (gridX >= 0 && gridX < GRID_SIZE && gridY >= 0 && gridY < GRID_SIZE) {
            if (grid[gridY][gridX] == WallType.NONE) {
                player.move(dx, dy);
            } else {
                player.setMovingAlongSilk(false);
                player.setCurrentMovingTarget(null);
                Gdx.app.log("GameScreen", "Hit wall");
            }
        }
    }
    
    /**
     * 检查是否在线段范围内
     */
    private boolean isWithinSilkRange(double newX, double newY) {
        if (player.getSilkQueue().isEmpty()) {
            return true;
        }
        
        for (ShootedSilk silk : player.getSilkQueue()) {
            if (!silk.isPlayerInRange(newX, newY)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 渲染游戏
     */
    private void renderGame() {
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // 绘制网格和墙壁
        drawGrid();
        
        game.shapeRenderer.end();
        
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        
        // 绘制线段
        drawSilks();
        
        game.shapeRenderer.end();
        
        game.shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // 绘制玩家
        drawPlayer();
        
        game.shapeRenderer.end();
    }
    
    /**
     * 绘制网格
     */
    private void drawGrid() {
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                float drawX = x * CELL_SIZE;
                float drawY = y * CELL_SIZE;
                
                if (grid[y][x] == WallType.NORMAL_WALL) {
                    game.shapeRenderer.setColor(Color.GRAY);
                    game.shapeRenderer.rect(drawX, drawY, CELL_SIZE, CELL_SIZE);
                } else if (grid[y][x] == WallType.SOLID_WALL) {
                    game.shapeRenderer.setColor(Color.DARK_GRAY);
                    game.shapeRenderer.rect(drawX, drawY, CELL_SIZE, CELL_SIZE);
                } else {
                    game.shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1);
                    game.shapeRenderer.rect(drawX, drawY, CELL_SIZE, CELL_SIZE);
                }
            }
        }
    }
    
    /**
     * 绘制线段
     */
    private void drawSilks() {
        game.shapeRenderer.setColor(player.getSilk().getColor());
        Gdx.gl.glLineWidth(player.getSilk().getWidth());
        
        for (ShootedSilk silk : player.getSilkQueue()) {
            float startX = (float) (silk.getXStart() * CELL_SIZE + CELL_SIZE / 2.0);
            float startY = (float) (silk.getYStart() * CELL_SIZE + CELL_SIZE / 2.0);
            float endX = (float) (silk.getXTarget() * CELL_SIZE + CELL_SIZE / 2.0);
            float endY = (float) (silk.getYTarget() * CELL_SIZE + CELL_SIZE / 2.0);
            
            game.shapeRenderer.line(startX, startY, endX, endY);
        }
        
        Gdx.gl.glLineWidth(1);
    }
    
    /**
     * 绘制玩家
     */
    private void drawPlayer() {
        float centerX = (float) (player.getGridX() * CELL_SIZE + CELL_SIZE / 2.0);
        float centerY = (float) (player.getGridY() * CELL_SIZE + CELL_SIZE / 2.0);
        
        // 绘制头部（圆圈）
        game.shapeRenderer.setColor(player.getColor());
        game.shapeRenderer.circle(centerX, centerY, CELL_SIZE / 3.0f, 20);
        
        // 绘制身体（根据方向）
        game.shapeRenderer.setColor(Color.NAVY);
        float bodySize = CELL_SIZE / 4.0f;
        
        switch (player.getDirection()) {
            case 0: // 上
                game.shapeRenderer.rect(centerX - bodySize / 2, centerY + CELL_SIZE / 3.0f, bodySize, bodySize);
                break;
            case 1: // 右
                game.shapeRenderer.rect(centerX + CELL_SIZE / 3.0f, centerY - bodySize / 2, bodySize, bodySize);
                break;
            case 2: // 下
                game.shapeRenderer.rect(centerX - bodySize / 2, centerY - CELL_SIZE / 3.0f - bodySize, bodySize, bodySize);
                break;
            case 3: // 左
                game.shapeRenderer.rect(centerX - CELL_SIZE / 3.0f - bodySize, centerY - bodySize / 2, bodySize, bodySize);
                break;
        }
    }
    
    /**
     * 更新 UI
     */
    private void updateUI() {
        positionLabel.setText(String.format("Position: (%.1f, %.1f)", player.getGridX(), player.getGridY()));
        silkInfoLabel.setText(String.format("Silk: %d/%d (Used: %d)", 
            player.getSilk().getRemainingLength(),
            player.getSilk().getTotalLength(),
            player.getSilk().getUsedLength()));
    }
    
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    
    @Override
    public void pause() {}
    
    @Override
    public void resume() {}
    
    @Override
    public void hide() {}
    
    @Override
    public void dispose() {
        stage.dispose();
    }
}
