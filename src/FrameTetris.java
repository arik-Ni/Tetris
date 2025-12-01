import javax.swing.*;
import java.awt.*;
import javax.swing.border.LineBorder;
import javax.swing.UIManager;

public class FrameTetris {

    // 定义统一的按钮尺寸
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 60;

    private static JFrame frame;
    private static JPanel menuPanel;

    // 辅助方法：快速创建具有相同风格的按钮
    private static JButton createStyledButton(String text) {
        JButton button = new JButton(text);

        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.setMinimumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.setMaximumSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        button.setForeground(Color.RED);
        button.setFont(new Font("SansSerif", Font.BOLD, 30));

        Color borderColor = Color.GREEN;
        int thickness = 5;
        LineBorder thickColoredBorder = new LineBorder(borderColor, thickness, true);
        button.setBorder(thickColoredBorder);

        return button;
    }

    //显示菜单界面

    private static void showMenu() {
        frame.getContentPane().removeAll();
        frame.add(menuPanel);
        frame.revalidate();
        frame.repaint();
    }

    //启动游戏

    private static void startGame() {
        // 创建游戏面板，传入返回菜单的回调
        GamePanel gamePanel = new GamePanel(() -> showMenu());

        frame.getContentPane().removeAll();
        frame.add(gamePanel);
        frame.revalidate();
        frame.repaint();

        // 给 GamePanel 焦点，使其能接收键盘输入
        gamePanel.requestFocusInWindow();
    }

    public static void main(String[] args){
        frame = new JFrame("Tetris");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 创建菜单面板
        menuPanel = new JPanel();

        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(Color.ORANGE);

        // 大标题
        JLabel titleLabel = new JLabel("T E T R I S");
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 130));
        titleLabel.setForeground(Color.BLUE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        menuPanel.add(Box.createVerticalStrut(70));
        menuPanel.add(titleLabel);
        menuPanel.add(Box.createVerticalStrut(110));

        // 创建所有按钮
        JButton buttonPlay = createStyledButton("Play");
        JButton buttonHowTo = createStyledButton("How to Play");
        JButton buttonCredits = createStyledButton("Credits");
        JButton buttonExit = createStyledButton("Exit");

        // 对齐方式
        buttonPlay.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonHowTo.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonCredits.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonExit.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 将按钮和垂直间距添加到面板中
        menuPanel.add(buttonPlay);
        menuPanel.add(Box.createVerticalStrut(40));
        menuPanel.add(buttonHowTo);
        menuPanel.add(Box.createVerticalStrut(40));
        menuPanel.add(buttonCredits);
        menuPanel.add(Box.createVerticalStrut(40));
        menuPanel.add(buttonExit);

        menuPanel.add(Box.createVerticalGlue());

        frame.add(menuPanel);

        // Play 按钮 - 启动游戏
        buttonPlay.addActionListener(e -> startGame());

        // How to Play 按钮，html代码居然也可也用！！！！
        buttonHowTo.addActionListener(e -> {
            Font newFont = new Font("SansSerif", Font.PLAIN, 28);
            UIManager.put("OptionPane.messageFont", newFont);
            String howToMessage = "<html>" +
                    "The game area is a grid where blocks fall from the top.<br><br>" +
                    "<b>Controls:</b><br>" +
                    "Move left/right: Use the left/right arrow keys<br>" +
                    "Rotate: Press the up/down arrow keys<br>" +
                    "Quick drop: Press spacebar or Enter<br>" +
                    "Return to menu: Press ESC<br><br>" +
                    "<b>Clearing Lines:</b><br>" +
                    "When a horizontal row is completely filled with blocks (no gaps), it disappears, and the blocks above it drop down.<br><br>" +
                    "<b>Game Over:</b><br>" +
                    "The game ends when new blocks can no longer enter the game area (i.e., the stack reaches the top).<br><br>" +
                    "<b>Tips:</b><br>" +
                    "Keep the game area as flat as possible.<br>" +
                    "Avoid leaving gaps, as they make it harder to place future blocks.<br>" +
                    "Use rotation to fit blocks into tight spaces." +
                    "</html>";
            JOptionPane.showMessageDialog(frame, howToMessage);
        });

        // Credits 按钮
        buttonCredits.addActionListener(e -> {
            Font newFont = new Font("SansSerif", Font.PLAIN, 28);
            UIManager.put("OptionPane.messageFont", newFont);
            String creditsMessage = "<html>" +
                    "<font size='+2'><b>Credits</b></font><br>" +
                    "<br>" +
                    "<font size='+1'><b>Game Development</b></font><br>" +
                    "Ni Yunbo<br>" +
                    "<font size='+1'><b>Programming</b></font><br>" +
                    "Ni Yunbo<br>" +
                    "<font size='+1'><b>Game Design</b></font><br>" +
                    "Ni Yunbo<br>" +
                    "<br>" +
                    "<b>Special Thanks</b><br>" +
                    "Claude AI<br>" +
                    "</html>";
            JOptionPane.showMessageDialog(frame, creditsMessage);
        });

        // Exit 按钮
        buttonExit.addActionListener(e -> {
            System.exit(0);
        });

        frame.setVisible(true);
    }
}