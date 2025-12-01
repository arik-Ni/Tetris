import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.LineBorder;
import java.util.ArrayList;
import java.util.List;

//GamePanel 类：处理俄罗斯方块的游戏逻辑和渲染
public class GamePanel extends JPanel {
    // 游戏网格参数
    private static final int GRID_WIDTH = 10;
    private static final int GRID_HEIGHT = 18;
    private static final int CELL_SIZE = 40;

    // 游戏状态
    private int[][] grid;
    private Tetromino currentTetromino;
    private Tetromino nextTetromino;
    private int currentX;
    private int currentY;
    private boolean gameOver = false;
    private int score = 0;

    // 计时器状态
    private long startTime; // 游戏开始或恢复时的时间戳
    private long totalElapsedTime = 0; // 累计经过的时间（毫秒）
    private boolean isPaused = false; // 暂停状态标记

    // 游戏循环
    private Timer gameTimer;
    private static final int GAME_SPEED = 500;

    // 延迟消除计时器和状态
    private Timer clearDelayTimer;
    private static final int CLEAR_DELAY = 500; // 0.5 秒
    private boolean isClearing = false; // 标记是否正在执行延迟清除（暂停主游戏）
    private List<Integer> linesToClear = new ArrayList<>(); // 存储待清除的行索引

    // 返回菜单的回调
    private Runnable returnToMenuCallback;

    // 网格绘制位置（居中计算）
    private int gridOffsetX;
    private int gridOffsetY;

    // 声明按钮
    private JButton btnRestart;
    private JButton btnExit;
    private JButton btnPause;
    private JButton btnGameOverReplay;
    private JButton btnGameOverExit;
    private boolean gameOverButtonsCreated = false;

    public GamePanel(Runnable returnToMenuCallback) {
        this.returnToMenuCallback = returnToMenuCallback;

        setBackground(new Color(30, 30, 30));
        setFocusable(true);
        setPreferredSize(new Dimension(800, 720));

        setLayout(null);

        setupControlPanel();

        initializeGame();

        setupKeyListener();

        startGameLoop();
    }

    //创建与主菜单风格一致的按钮

    private JButton createGameStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setForeground(Color.RED);
        button.setFont(new Font("SansSerif", Font.BOLD, 25));

        Color borderColor = Color.GREEN;
        int thickness = 3;
        LineBorder thickColoredBorder = new LineBorder(borderColor, thickness, true);
        button.setBorder(thickColoredBorder);

        button.setPreferredSize(new Dimension(120, 40));
        return button;
    }

    //设置左上角控制按钮面板

    private void setupControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 15));
        controlPanel.setOpaque(false);

        btnRestart = createGameStyledButton("Refresh");
        btnRestart.addActionListener(e -> showRestartConfirmation());

        btnPause = createGameStyledButton("Pause");
        btnPause.addActionListener(e -> togglePause());

        btnExit = createGameStyledButton("Exit");
        btnExit.addActionListener(e -> showExitConfirmation());

        controlPanel.add(btnRestart);
        controlPanel.add(btnPause);
        controlPanel.add(btnExit);

        this.add(controlPanel);
        controlPanel.setBounds(0, 0, 450, 60);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                controlPanel.setBounds(0, 0, 450, 60);
            }
        });
    }

    //暂停/恢复游戏

    private void togglePause() {
        if (gameOver || isClearing) return; // 消除延迟期间不能暂停

        if (gameTimer.isRunning()) {
            gameTimer.stop();
            btnPause.setText("Resume");

            // 计时器/暂停逻辑，累积时间
            totalElapsedTime += (System.currentTimeMillis() - startTime);
            isPaused = true;
        } else {
            gameTimer.start();
            btnPause.setText("Pause");

            // 计时器/恢复逻辑，重置起始时间
            startTime = System.currentTimeMillis();
            isPaused = false;
        }
        requestFocusInWindow();
        repaint();
    }

    //初始化游戏

    private void initializeGame() {
        grid = new int[GRID_HEIGHT][GRID_WIDTH];

        // 明确初始化 current 和 next Tetromino
        currentTetromino = Tetromino.createRandom();
        nextTetromino = Tetromino.createRandom();

        currentX = GRID_WIDTH / 2 - 1;
        currentY = 0;

        if (!isValidPosition(currentX, currentY, currentTetromino)) {
            gameOver = true;
        }

        // 计时器初始化
        startTime = System.currentTimeMillis();
        totalElapsedTime = 0;
        isPaused = false;
        isClearing = false;
        linesToClear.clear();
    }

    //清空并重玩当前游戏

    private void restartGame() {
        // 隐藏 Game Over 按钮
        if (btnGameOverReplay != null) {
            this.remove(btnGameOverReplay);
            this.remove(btnGameOverExit);
            gameOverButtonsCreated = false;
        }

        grid = new int[GRID_HEIGHT][GRID_WIDTH];
        score = 0;
        gameOver = false;

        // 明确创建当前方块和下一个方块
        currentTetromino = Tetromino.createRandom();
        nextTetromino = Tetromino.createRandom();

        currentX = GRID_WIDTH / 2 - 1;
        currentY = 0;

        // 检查初始方块是否合法
        if (!isValidPosition(currentX, currentY, currentTetromino)) {
            gameOver = true;
        }

        if (gameTimer != null && !gameTimer.isRunning()) {
            gameTimer.start();
        }
        btnPause.setText("Pause");

        // 计时器重置
        startTime = System.currentTimeMillis();
        totalElapsedTime = 0;
        isPaused = false;
        isClearing = false;
        linesToClear.clear();

        requestFocusInWindow();
        repaint();
    }

    //处理刷新按钮点击时的确认对话框

    private void showRestartConfirmation() {
        boolean wasRunning = gameTimer.isRunning();
        if (wasRunning) {
            gameTimer.stop();
        }

        String[] options = {"Yes", "No"};
        int result = JOptionPane.showOptionDialog(
                this,
                "Are you sure you want to restart?",
                "Confirm Restart",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[0]
        );

        if (result == JOptionPane.YES_OPTION) {
            restartGame();
        } else {
            if (wasRunning) {
                gameTimer.start();
            }
        }
        requestFocusInWindow();
    }

    //处理退出按钮/ESC按下的确认对话框

    private void showExitConfirmation() {
        boolean wasRunning = gameTimer.isRunning();
        if (wasRunning) {
            gameTimer.stop();
        }

        String[] options = {"Yes", "No"};
        int result = JOptionPane.showOptionDialog(
                this,
                "Are you sure you want to exit and return to menu?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[0]
        );

        if (result == JOptionPane.YES_OPTION) {
            returnToMenu();
        } else {
            if (wasRunning) {
                gameTimer.start();
            }
        }
        requestFocusInWindow();
    }

    private void calculateGridOffset() {
        int gridPixelWidth = GRID_WIDTH * CELL_SIZE;
        int gridPixelHeight = GRID_HEIGHT * CELL_SIZE;

        gridOffsetX = (getWidth() - gridPixelWidth) / 2;
        gridOffsetY = (getHeight() - gridPixelHeight) / 2;
    }

    private void spawnNewTetromino() {
        currentTetromino = nextTetromino;
        nextTetromino = Tetromino.createRandom();

        currentX = GRID_WIDTH / 2 - 1;
        currentY = 0;

        if (!isValidPosition(currentX, currentY, currentTetromino)) {
            gameOver = true;
            gameTimer.stop(); // 游戏结束时停止计时器
            btnPause.setText("Pause");
        }
    }

    private boolean isValidPosition(int x, int y, Tetromino tetromino) {
        int[][] shape = tetromino.getShape();
        int shapeSize = shape.length;

        for (int r = 0; r < shapeSize; r++) {
            for (int c = 0; c < shapeSize; c++) {
                if (shape[r][c] == 1) {
                    int gridX = x + c;
                    int gridY = y + r;

                    if (gridX < 0 || gridX >= GRID_WIDTH || gridY >= GRID_HEIGHT) {
                        return false;
                    }

                    if (gridY >= 0 && grid[gridY][gridX] != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    //方块锁定逻辑：现在在检测到满行时，会启动延迟消除计时器

    private void lockTetromino() {
        int[][] shape = currentTetromino.getShape();
        int shapeSize = shape.length;
        int colorValue = currentTetromino.getColor().getRGB();

        for (int r = 0; r < shapeSize; r++) {
            for (int c = 0; c < shapeSize; c++) {
                if (shape[r][c] == 1) {
                    int gridX = currentX + c;
                    int gridY = currentY + r;
                    if (gridY >= 0 && gridY < GRID_HEIGHT && gridX >= 0 && gridX < GRID_WIDTH) {
                        grid[gridY][gridX] = colorValue;
                    }
                }
            }
        }

        int linesCleared = detectCompleteLines();

        if (linesCleared > 0) {
            // 暂停游戏下落
            gameTimer.stop();
            isClearing = true;

            // 启动延迟计时器
            clearDelayTimer = new Timer(CLEAR_DELAY, e -> {
                // 延迟结束后执行实际消除
                clearDelayTimer.stop();
                actuallyClearLines();
                isClearing = false;

                // 消除完成后生成新方块并重启游戏
                spawnNewTetromino();
                gameTimer.start();
                btnPause.setText("Pause"); // 确保暂停按钮文本正确显示
                repaint();
            });
            clearDelayTimer.setRepeats(false); // 只执行一次
            clearDelayTimer.start();

        } else {
            // 没有满行则直接生成下一个方块
            spawnNewTetromino();
        }
    }

    //只检测满行，并将满行索引存储到 linesToClear 列表中

    private int detectCompleteLines() {
        linesToClear.clear();
        for (int row = GRID_HEIGHT - 1; row >= 0; row--) {
            boolean isComplete = true;
            for (int col = 0; col < GRID_WIDTH; col++) {
                if (grid[row][col] == 0) {
                    isComplete = false;
                    break;
                }
            }

            if (isComplete) {
                linesToClear.add(row);
            }
        }
        return linesToClear.size();
    }

    //实际执行消除操作和加分

    private void actuallyClearLines() {
        if (linesToClear.isEmpty()) return;

        // 从下往上清除，以确保索引正确
        linesToClear.sort((a, b) -> b - a);

        for (int row : linesToClear) {
            // 向上移动上方所有行
            for (int r = row; r > 0; r--) {
                grid[r] = grid[r - 1].clone();
            }
            // 顶部创建新空行
            grid[0] = new int[GRID_WIDTH];
            score += 100;
        }
        linesToClear.clear();
    }

    private void moveDown() {
        if (gameOver || isClearing) return; // 消除延迟期间不能移动

        if (isValidPosition(currentX, currentY + 1, currentTetromino)) {
            currentY++;
        } else {
            lockTetromino();
        }
    }
    private void moveLeft() {
        if (gameOver || isClearing) return;
        if (isValidPosition(currentX - 1, currentY, currentTetromino)) {
            currentX--;
        }
    }
    private void moveRight() {
        if (gameOver || isClearing) return;
        if (isValidPosition(currentX + 1, currentY, currentTetromino)) {
            currentX++;
        }
    }
    private void rotate() {
        if (gameOver || isClearing) return;
        Tetromino rotated = currentTetromino.rotate();
        if (isValidPosition(currentX, currentY, rotated)) {
            currentTetromino = rotated;
        }
    }
    private void fastDrop() {
        if (gameOver || isClearing) return;
        while (isValidPosition(currentX, currentY + 1, currentTetromino)) {
            currentY++;
        }
        lockTetromino();
    }

    private void returnToMenu() {
        if (gameTimer != null) gameTimer.stop();
        if (clearDelayTimer != null) clearDelayTimer.stop(); // 停止所有计时器
        if (returnToMenuCallback != null) {
            returnToMenuCallback.run();
        }
    }

    private void setupKeyListener() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    showExitConfirmation();
                    return;
                }

                // 只有在游戏运行且不处于清除延迟状态时才响应操作
                if (gameTimer.isRunning() && !gameOver && !isClearing) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_LEFT:
                            moveLeft();
                            break;
                        case KeyEvent.VK_RIGHT:
                            moveRight();
                            break;
                        case KeyEvent.VK_UP:
                        case KeyEvent.VK_DOWN:
                            rotate();
                            break;
                        case KeyEvent.VK_SPACE:
                        case KeyEvent.VK_ENTER:
                            fastDrop();
                            break;
                    }
                }

                repaint();
            }
        });
    }

    private void startGameLoop() {
        gameTimer = new Timer(GAME_SPEED, e -> {
            if (!gameOver && !isClearing) { // 延迟清除期间不执行下落
                moveDown();
                repaint();
            }
        });
        gameTimer.start();
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        calculateGridOffset();

        drawGrid(g2d);
        drawLockedTetrominos(g2d);
        drawCurrentTetromino(g2d);

        drawNextTetromino(g2d);

        drawTimer(g2d);

        drawScore(g2d);

        if (gameOver) {
            drawGameOverMessage(g2d);
        }
    }

    //绘制游戏计时器

    private void drawTimer(Graphics2D g2d) {
        //  获取当前累计时间
        long currentTime;
        // 延迟清除期间也算作暂停状态
        if (isPaused || gameOver || isClearing) {
            currentTime = totalElapsedTime;
        } else {
            currentTime = totalElapsedTime + (System.currentTimeMillis() - startTime);
        }

        // 格式化时间 (HH:MM:SS)
        long seconds = currentTime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds %= 60;
        minutes %= 60;

        String timeStr = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        // 计算位置和大小 (继承自 Next 框)
        int previewWidth = 4 * CELL_SIZE;
        int previewHeight = 4 * CELL_SIZE;
        int previewX = gridOffsetX + (GRID_WIDTH * CELL_SIZE) + 30;
        int previewY = gridOffsetY;

        int timerBoxWidth = previewWidth;
        int timerBoxHeight = CELL_SIZE + 10;
        int verticalGap = 100;
        int timerBoxX = previewX;
        int timerBoxY = previewY + previewHeight + verticalGap;

        // 绘制标题 "TIMER"
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("TIMER", timerBoxX, timerBoxY - 10);

        //  绘制背景和边框
        g2d.setColor(new Color(50, 50, 50));
        g2d.fillRect(timerBoxX, timerBoxY, timerBoxWidth, timerBoxHeight);

        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(timerBoxX, timerBoxY, timerBoxWidth, timerBoxHeight);

        //  绘制时间文本
        g2d.setFont(new Font("Arial", Font.BOLD, 30));
        g2d.setColor(Color.YELLOW);

        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(timeStr);
        int ascent = fm.getAscent();

        // 居中显示文本
        int textX = timerBoxX + (timerBoxWidth - textWidth) / 2;
        int textY = timerBoxY + (timerBoxHeight + ascent) / 2 - 5;

        g2d.drawString(timeStr, textX, textY);
    }

    private void drawNextTetromino(Graphics2D g2d) {
        if (nextTetromino == null) return;

        int previewWidth = 4 * CELL_SIZE;
        int previewHeight = 4 * CELL_SIZE;
        int previewX = gridOffsetX + (GRID_WIDTH * CELL_SIZE) + 30;
        int previewY = gridOffsetY;

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("NEXT", previewX, previewY - 10);

        g2d.setColor(new Color(50, 50, 50));
        g2d.fillRect(previewX, previewY, previewWidth, previewHeight);

        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(previewX, previewY, previewWidth, previewHeight);

        int[][] shape = nextTetromino.getShape();
        g2d.setColor(nextTetromino.getColor());

        int cellOffset = shape.length == 4 ? 0 : (CELL_SIZE / 2);

        for (int r = 0; r < shape.length; r++) {
            for (int c = 0; c < shape.length; c++) {
                if (shape[r][c] == 1) {
                    int x = previewX + cellOffset + c * CELL_SIZE;
                    int y = previewY + cellOffset + r * CELL_SIZE;

                    g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                    g2d.setColor(Color.BLACK);
                    g2d.drawRect(x, y, CELL_SIZE, CELL_SIZE);
                    g2d.setColor(nextTetromino.getColor());
                }
            }
        }
    }

    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.fillRect(gridOffsetX, gridOffsetY, GRID_WIDTH * CELL_SIZE, GRID_HEIGHT * CELL_SIZE);

        g2d.setColor(new Color(80, 80, 80));
        for (int row = 0; row <= GRID_HEIGHT; row++) {
            g2d.drawLine(gridOffsetX, gridOffsetY + row * CELL_SIZE,
                    gridOffsetX + GRID_WIDTH * CELL_SIZE, gridOffsetY + row * CELL_SIZE);
        }
        for (int col = 0; col <= GRID_WIDTH; col++) {
            g2d.drawLine(gridOffsetX + col * CELL_SIZE, gridOffsetY,
                    gridOffsetX + col * CELL_SIZE, gridOffsetY + GRID_HEIGHT * CELL_SIZE);
        }

        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRect(gridOffsetX, gridOffsetY, GRID_WIDTH * CELL_SIZE, GRID_HEIGHT * CELL_SIZE);
    }

    private void drawLockedTetrominos(Graphics2D g2d) {
        for (int row = 0; row < GRID_HEIGHT; row++) {
            for (int col = 0; col < GRID_WIDTH; col++) {
                if (grid[row][col] != 0) {
                    int x = gridOffsetX + col * CELL_SIZE;
                    int y = gridOffsetY + row * CELL_SIZE;

                    g2d.setColor(new Color(grid[row][col]));
                    g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                    g2d.setColor(Color.BLACK);
                    g2d.drawRect(x, y, CELL_SIZE, CELL_SIZE);
                }
            }
        }
    }

    private void drawCurrentTetromino(Graphics2D g2d) {
        if (isClearing) return; // 延迟清除期间不绘制下落方块

        int[][] shape = currentTetromino.getShape();
        int shapeSize = shape.length;

        g2d.setColor(currentTetromino.getColor());
        for (int r = 0; r < shapeSize; r++) {
            for (int c = 0; c < shapeSize; c++) {
                if (shape[r][c] == 1) {
                    int x = gridOffsetX + (currentX + c) * CELL_SIZE;
                    int y = gridOffsetY + (currentY + r) * CELL_SIZE;
                    if (y >= gridOffsetY) {
                        g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                        g2d.setColor(Color.BLACK);
                        g2d.drawRect(x, y, CELL_SIZE, CELL_SIZE);
                        g2d.setColor(currentTetromino.getColor());
                    }
                }
            }
        }
    }

    private void drawScore(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 28));

        String scoreText = "Score: " + score;
        FontMetrics fm = g2d.getFontMetrics();

        int margin = 30;
        int x = getWidth() - fm.stringWidth(scoreText) - margin;
        int y = margin + fm.getAscent();

        g2d.drawString(scoreText, x, y);
    }

    // 创建 Game Over 界面的按钮
    private void createGameOverButtons() {
        btnGameOverReplay = createGameStyledButton("Replay");
        btnGameOverReplay.addActionListener(e -> restartGame());

        btnGameOverExit = createGameStyledButton("Exit");
        btnGameOverExit.addActionListener(e -> showExitConfirmation());

        // 计算按钮位置（在 Game Over 消息下方）
        int buttonWidth = 120;
        int buttonHeight = 40;
        int gap = 20;
        int totalWidth = buttonWidth * 2 + gap;

        int centerX = (getWidth() - totalWidth) / 2;
        int centerY = getHeight() / 2 + 150;

        btnGameOverReplay.setBounds(centerX, centerY, buttonWidth, buttonHeight);
        btnGameOverExit.setBounds(centerX + buttonWidth + gap, centerY, buttonWidth, buttonHeight);

        this.add(btnGameOverReplay);
        this.add(btnGameOverExit);
    }

    private void drawGameOverMessage(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(Color.RED);
        g2d.setFont(new Font("Arial", Font.BOLD, 60));
        String gameOverText = "GAME OVER";
        FontMetrics fm = g2d.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(gameOverText)) / 2;
        int y = getHeight() / 2 - 20;
        g2d.drawString(gameOverText, x, y);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.PLAIN, 32));
        String scoreText = "Final Score: " + score;
        fm = g2d.getFontMetrics();
        x = (getWidth() - fm.stringWidth(scoreText)) / 2;
        g2d.drawString(scoreText, x, y + 90);

        // 创建按钮（只创建一次）
        if (!gameOverButtonsCreated) {
            createGameOverButtons();
            gameOverButtonsCreated = true;
        }
    }

    public int getScore() {
        return score;
    }

    public boolean isGameOver() {
        return gameOver;
    }
}