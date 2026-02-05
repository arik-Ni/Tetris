import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.LineBorder;
import java.util.ArrayList;
import java.util.List;

// 游戏主面板，管逻辑也管画图 | Le cœur du jeu : logique et rendu
public class GamePanel extends JPanel {
    // 棋盘格大小，10x18是经典比例 | Taille de la grille, 10x18 c'est le standard
    private static final int GRID_WIDTH = 10;
    private static final int GRID_HEIGHT = 18;
    private static final int CELL_SIZE = 40;

    // 游戏核心变量 | Variables d'état
    private int[][] grid;
    private Tetromino currentTetromino;
    private Tetromino nextTetromino;
    private int currentX;
    private int currentY;
    private boolean gameOver = false;
    private int score = 0;

    // 计时器相关，算累计时间用的 | Gestion du temps et chrono
    private long startTime; // 记录开始或恢复那一刻的时间戳 Horodatage de début
    private long totalElapsedTime = 0; // 存档已经跑了多久 Temps cumulé
    private boolean isPaused = false; // 暂停开关

    // 游戏下落的主循环 | Timer principal pour la chute
    private Timer gameTimer;
    private static final int GAME_SPEED = 500; // 0.5秒掉一格，刚好 Vitesse de chute (0.5s)

    // 消行时的停顿效果，不然闪太快看不清 | Délai d'effacement pour l'animation
    private Timer clearDelayTimer;
    private static final int CLEAR_DELAY = 500; // 留半秒给玩家反应 0.5s de pause
    private boolean isClearing = false; // 正在播动画时得锁住键盘 Bloquer les actions pendant l'animation
    private List<Integer> linesToClear = new ArrayList<>(); // 攒着哪些行该炸了 Lignes à supprimer

    // 往回跳菜单的回调 | Callback pour retourner au menu
    private Runnable returnToMenuCallback;

    // 居中显示时算出的偏移量 | Offsets pour centrer la grille
    private int gridOffsetX;
    private int gridOffsetY;

    // 各种按钮组件 | Les boutons de l'interface
    private JButton btnRestart;
    private JButton btnExit;
    private JButton btnPause;
    private JButton btnGameOverReplay;
    private JButton btnGameOverExit;
    private boolean gameOverButtonsCreated = false;

    public GamePanel(Runnable returnToMenuCallback) {
        this.returnToMenuCallback = returnToMenuCallback;

        setBackground(new Color(30, 30, 30)); // 背景深色点，护眼 Mode sombre
        setFocusable(true);
        setPreferredSize(new Dimension(800, 720));

        setLayout(null); // 手动布局，方便控制位置 Layout manuel

        setupControlPanel();

        initializeGame();

        setupKeyListener();

        startGameLoop();
    }

    // 按钮样式统一一下，省得写重复代码 | Style commun pour les boutons du jeu

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

    // 顶上那一排控制按钮 | Barre d'outils en haut

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

        // 窗口大小变了得跟着动 | Ajustement si on redimensionne
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                controlPanel.setBounds(0, 0, 450, 60);
            }
        });
    }

    // 暂停/继续切换逻辑 | Basculer entre pause et jeu

    private void togglePause() {
        if (gameOver || isClearing) return; // 正在炸行或者已经挂了就别点了

        if (gameTimer.isRunning()) {
            gameTimer.stop();
            btnPause.setText("Resume");

            // 停下那一刻，把这段时间存起来 Accumuler le temps écoulé
            totalElapsedTime += (System.currentTimeMillis() - startTime);
            isPaused = true;
        } else {
            gameTimer.start();
            btnPause.setText("Pause");

            // 重新开始跑，重置起点时间 Reset le point de départ
            startTime = System.currentTimeMillis();
            isPaused = false;
        }
        requestFocusInWindow(); // 焦点拿回来，不然键盘没反应 Focus pour le clavier
        repaint();
    }

    // 开局初始化 | Setup initial du jeu

    private void initializeGame() {
        grid = new int[GRID_HEIGHT][GRID_WIDTH];

        // 随机刷两个块出来 Générer les premiers blocs
        currentTetromino = Tetromino.createRandom();
        nextTetromino = Tetromino.createRandom();

        currentX = GRID_WIDTH / 2 - 1; // 居中生成 Position de départ
        currentY = 0;

        // 一出生就撞墙，说明顶满了，Game Over dès le début
        if (!isValidPosition(currentX, currentY, currentTetromino)) {
            gameOver = true;
        }

        startTime = System.currentTimeMillis();
        totalElapsedTime = 0;
        isPaused = false;
        isClearing = false;
        linesToClear.clear();
    }

    // 推倒重来 | Reset complet de la partie

    private void restartGame() {
        // 清理掉结算界面的按钮 Nettoyer l'interface de fin
        if (btnGameOverReplay != null) {
            this.remove(btnGameOverReplay);
            this.remove(btnGameOverExit);
            gameOverButtonsCreated = false;
        }

        grid = new int[GRID_HEIGHT][GRID_WIDTH];
        score = 0;
        gameOver = false;

        currentTetromino = Tetromino.createRandom();
        nextTetromino = Tetromino.createRandom();

        currentX = GRID_WIDTH / 2 - 1;
        currentY = 0;

        if (!isValidPosition(currentX, currentY, currentTetromino)) {
            gameOver = true;
        }

        if (gameTimer != null && !gameTimer.isRunning()) {
            gameTimer.start();
        }
        btnPause.setText("Pause");

        // 计时也得归零 Reset chrono
        startTime = System.currentTimeMillis();
        totalElapsedTime = 0;
        isPaused = false;
        isClearing = false;
        linesToClear.clear();

        requestFocusInWindow();
        repaint();
    }

    // 刷新确认确认，防止手滑 | Popup de confirmation pour recommencer

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
                gameTimer.start(); // 点错了就继续跑 Reprendre si annulé
            }
        }
        requestFocusInWindow();
    }

    // 退出确认，回主菜单 | Retour au menu principal

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

    // 算一下格子画在哪能居中 | Centrer la zone de jeu
    private void calculateGridOffset() {
        int gridPixelWidth = GRID_WIDTH * CELL_SIZE;
        int gridPixelHeight = GRID_HEIGHT * CELL_SIZE;

        gridOffsetX = (getWidth() - gridPixelWidth) / 2;
        gridOffsetY = (getHeight() - gridPixelHeight) / 2;
    }

    // 下一个块顶上去，再抽个新的 | Nouveau bloc et mise à jour de la prévisualisation
    private void spawnNewTetromino() {
        currentTetromino = nextTetromino;
        nextTetromino = Tetromino.createRandom();

        currentX = GRID_WIDTH / 2 - 1;
        currentY = 0;

        if (!isValidPosition(currentX, currentY, currentTetromino)) {
            gameOver = true;
            gameTimer.stop();
            btnPause.setText("Pause");
        }
    }

    // 碰撞检测核心，别让方块钻墙里 | Détection de collision
    private boolean isValidPosition(int x, int y, Tetromino tetromino) {
        int[][] shape = tetromino.getShape();
        int shapeSize = shape.length;

        for (int r = 0; r < shapeSize; r++) {
            for (int c = 0; c < shapeSize; c++) {
                if (shape[r][c] == 1) {
                    int gridX = x + c;
                    int gridY = y + r;

                    // 越界检查 Check les limites
                    if (gridX < 0 || gridX >= GRID_WIDTH || gridY >= GRID_HEIGHT) {
                        return false;
                    }

                    // 撞到已经固定的块了 Check les blocs fixés
                    if (gridY >= 0 && grid[gridY][gridX] != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // 落地锁死逻辑：如果有消行，得先停一下播动画 | Verrouillage du bloc et déclenchement de l'effacement

    private void lockTetromino() {
        int[][] shape = currentTetromino.getShape();
        int shapeSize = shape.length;
        int colorValue = currentTetromino.getColor().getRGB();

        // 把方块颜色填进网格里 Fixer les pixels
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
            gameTimer.stop(); // 别往下掉了，先处理动画 Stop la chute pour l'animation
            isClearing = true;

            // 延迟半秒再清，让玩家看一眼哪行炸了
            clearDelayTimer = new Timer(CLEAR_DELAY, e -> {
                clearDelayTimer.stop();
                actuallyClearLines(); // 动手清行
                isClearing = false;

                spawnNewTetromino();
                gameTimer.start(); // 搞定，继续掉 Nouveau bloc et on reprend
                btnPause.setText("Pause");
                repaint();
            });
            clearDelayTimer.setRepeats(false);
            clearDelayTimer.start();

        } else {
            spawnNewTetromino(); // 没消行就直接出下一个
        }
    }

    // 看看哪些行填满了，记下索引 | Identifier les lignes pleines

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

    // 真正动手删行、挪位置、加分 | Suppression réelle des lignes et calcul du score

    private void actuallyClearLines() {
        if (linesToClear.isEmpty()) return;

        // 必须从下往上删，不然索引全乱了 Trier du bas vers le haut
        linesToClear.sort((a, b) -> b - a);

        for (int row : linesToClear) {
            // 上面的行往下挤一行 Tout décaler vers le bas
            for (int r = row; r > 0; r--) {
                grid[r] = grid[r - 1].clone();
            }
            grid[0] = new int[GRID_WIDTH]; // 最顶上补个空的
            score += 100; // 一行100分，简单粗暴 100 points par ligne
        }
        linesToClear.clear();
    }

    // 各种位移逻辑，都要检查碰撞 | Fonctions de mouvement
    private void moveDown() {
        if (gameOver || isClearing) return;

        if (isValidPosition(currentX, currentY + 1, currentTetromino)) {
            currentY++;
        } else {
            lockTetromino(); // 到底了，锁死
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

    // 收尾工作，关掉计时器 | Arrêter les timers avant de partir
    private void returnToMenu() {
        if (gameTimer != null) gameTimer.stop();
        if (clearDelayTimer != null) clearDelayTimer.stop();
        if (returnToMenuCallback != null) {
            returnToMenuCallback.run();
        }
    }

    // 键盘监听，ESC随时准备跑路 | Gestion des touches clavier
    private void setupKeyListener() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    showExitConfirmation();
                    return;
                }

                // 没暂停、没挂、没播动画时才理你 Seul actif si le jeu tourne
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

    // 游戏心脏跳动的地方 | Le cœur du jeu (loop)
    private void startGameLoop() {
        gameTimer = new Timer(GAME_SPEED, e -> {
            if (!gameOver && !isClearing) {
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
        // 开启抗锯齿，看着舒服点 Antialiasing pour le confort
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

    // 画计时器，算时间有点绕 | Dessin du chrono, calcul un peu tricky

    private void drawTimer(Graphics2D g2d) {
        long currentTime;
        // 只有在真正跑的时候才算实时时间 Calcul du temps réel vs temps figé
        if (isPaused || gameOver || isClearing) {
            currentTime = totalElapsedTime;
        } else {
            currentTime = totalElapsedTime + (System.currentTimeMillis() - startTime);
        }

        // 把毫秒转成 00:00:00 格式 Formatage HH:MM:SS
        long seconds = currentTime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        seconds %= 60;
        minutes %= 60;

        String timeStr = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        // UI 布局位置，跟 Next 框对齐 | Position alignée sur l'aperçu du prochain bloc
        int previewWidth = 4 * CELL_SIZE;
        int previewHeight = 4 * CELL_SIZE;
        int previewX = gridOffsetX + (GRID_WIDTH * CELL_SIZE) + 30;
        int previewY = gridOffsetY;

        int timerBoxWidth = previewWidth;
        int timerBoxHeight = CELL_SIZE + 10;
        int verticalGap = 100;
        int timerBoxX = previewX;
        int timerBoxY = previewY + previewHeight + verticalGap;

        // 画标题和框框 Dessin de l'interface du timer
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("TIMER", timerBoxX, timerBoxY - 10);

        g2d.setColor(new Color(50, 50, 50));
        g2d.fillRect(timerBoxX, timerBoxY, timerBoxWidth, timerBoxHeight);

        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRect(timerBoxX, timerBoxY, timerBoxWidth, timerBoxHeight);

        // 时间数字用黄色，醒目点 Jaune pour le temps
        g2d.setFont(new Font("Arial", Font.BOLD, 30));
        g2d.setColor(Color.YELLOW);

        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(timeStr);
        int ascent = fm.getAscent();

        // 完美居中 Centrage précis
        int textX = timerBoxX + (timerBoxWidth - textWidth) / 2;
        int textY = timerBoxY + (timerBoxHeight + ascent) / 2 - 5;

        g2d.drawString(timeStr, textX, textY);
    }

    // 画预览块，提示玩家下一步该怎么走 | Affichage du prochain bloc (NEXT)
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

        // 4x4 的块和 3x3 的块偏移量不同，得微调一下 Ajustement selon la forme
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

    // 画背景网格，辅助对齐 | La grille de fond
    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.fillRect(gridOffsetX, gridOffsetY, GRID_WIDTH * CELL_SIZE, GRID_HEIGHT * CELL_SIZE);

        // 细灰线，别太亮 Lignes discrètes
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

    // 画那些已经躺平的块 | Dessin des blocs déjà posés
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

    // 画正在掉的那一个块 | Dessin du bloc en mouvement
    private void drawCurrentTetromino(Graphics2D g2d) {
        if (isClearing) return; // 正在播消行动画时，先把它藏起来 Cache pendant l'animation

        int[][] shape = currentTetromino.getShape();
        int shapeSize = shape.length;

        g2d.setColor(currentTetromino.getColor());
        for (int r = 0; r < shapeSize; r++) {
            for (int c = 0; c < shapeSize; c++) {
                if (shape[r][c] == 1) {
                    int x = gridOffsetX + (currentX + c) * CELL_SIZE;
                    int y = gridOffsetY + (currentY + r) * CELL_SIZE;
                    if (y >= gridOffsetY) { // 没出顶部的部分不画 Ne pas dessiner hors écran
                        g2d.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                        g2d.setColor(Color.BLACK);
                        g2d.drawRect(x, y, CELL_SIZE, CELL_SIZE);
                        g2d.setColor(currentTetromino.getColor());
                    }
                }
            }
        }
    }

    // 分数显示在右上角 | Score affiché en haut à droite
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

    // 挂了之后的结算界面按钮 | Boutons post-défaite
    private void createGameOverButtons() {
        btnGameOverReplay = createGameStyledButton("Replay");
        btnGameOverReplay.addActionListener(e -> restartGame());

        btnGameOverExit = createGameStyledButton("Exit");
        btnGameOverExit.addActionListener(e -> showExitConfirmation());

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

    // 游戏结束的遮罩和提示词 | Message Game Over et overlay
    private void drawGameOverMessage(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 180)); // 屏幕黑一下，有氛围
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