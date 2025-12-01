import java.awt.Color;
import java.util.Random;

/*
 Tetromino 类：定义单个俄罗斯方块的形状、颜色和旋转状态。
 每个方块由一个 2D 数组定义其相对于自身的坐标。
 */
public class Tetromino {
    // 方块类型常量
    public static final int TYPE_I = 1;
    public static final int TYPE_J = 2;
    public static final int TYPE_L = 3;
    public static final int TYPE_O = 4;
    public static final int TYPE_S = 5;
    public static final int TYPE_T = 6;
    public static final int TYPE_Z = 7;

    private int type;
    private int[][] shape; // 形状数组，1表示方块实体，0表示空
    private Color color;

    // 私有构造函数，只能通过静态工厂方法创建
    private Tetromino(int type, int[][] shape, Color color) {
        this.type = type;
        this.shape = shape;
        this.color = color;
    }

    // 静态工厂方法：创建所有七种标准方块
    // (使用 4x4 数组定义 I 型，3x3 数组定义其他，这是常见的做法)

    public static Tetromino createI() {
        // I 型 (青色/Cyan)
        int[][] iShape = new int[][] {
                {0, 0, 0, 0},
                {1, 1, 1, 1},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        };
        return new Tetromino(TYPE_I, iShape, Color.CYAN);
    }

    public static Tetromino createJ() {
        // J 型 (蓝色/Blue)
        int[][] jShape = new int[][] {
                {1, 0, 0},
                {1, 1, 1},
                {0, 0, 0}
        };
        return new Tetromino(TYPE_J, jShape, Color.BLUE);
    }

    public static Tetromino createL() {
        // L 型 (橙色/Orange)
        int[][] lShape = new int[][] {
                {0, 0, 1},
                {1, 1, 1},
                {0, 0, 0}
        };
        return new Tetromino(TYPE_L, lShape, Color.ORANGE);
    }

    public static Tetromino createO() {
        // O 型 (黄色/Yellow) - 只需要 2x2 数组
        int[][] oShape = new int[][] {
                {1, 1},
                {1, 1}
        };
        return new Tetromino(TYPE_O, oShape, Color.YELLOW);
    }

    public static Tetromino createS() {
        // S 型 (绿色/Green)
        int[][] sShape = new int[][] {
                {0, 1, 1},
                {1, 1, 0},
                {0, 0, 0}
        };
        return new Tetromino(TYPE_S, sShape, Color.GREEN);
    }

    public static Tetromino createT() {
        // T 型 (洋红色/Magenta)
        int[][] tShape = new int[][] {
                {0, 1, 0},
                {1, 1, 1},
                {0, 0, 0}
        };
        return new Tetromino(TYPE_T, tShape, Color.MAGENTA);
    }

    public static Tetromino createZ() {
        // Z 型 (红色/Red)
        int[][] zShape = new int[][] {
                {1, 1, 0},
                {0, 1, 1},
                {0, 0, 0}
        };
        return new Tetromino(TYPE_Z, zShape, Color.RED);
    }

    // 实用方法：随机生成一个方块
    private static final Random random = new Random();

    public static Tetromino createRandom() {
        // 随机选择一个类型 (1到7)
        int randomType = random.nextInt(7) + 1;

        switch (randomType) {
            case TYPE_I: return createI();
            case TYPE_J: return createJ();
            case TYPE_L: return createL();
            case TYPE_O: return createO();
            case TYPE_S: return createS();
            case TYPE_T: return createT();
            case TYPE_Z: return createZ();
            default: return createT(); // 默认返回 T 型（作为安全备份）
        }
    }

    // 旋转逻辑 (TODO: 复杂的核心逻辑)

    /**
      返回一个新对象，该新对象是当前方块顺时针旋转 90 度后的状态。
      注意：在实际游戏中，您必须在 GamePanel 中验证旋转后的位置是否合法。
     */
    public Tetromino rotate() {
        // 旋转的核心算法：
        // 设原形状为 shape[r][c]，旋转后的新形状 newShape[r][c]
        // 顺时针旋转 90 度的关系是：newShape[c][N - 1 - r] = shape[r][c]

        int N = shape.length; // 形状矩阵的大小 ，3 或 4
        int[][] newShape = new int[N][N];

        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                // 执行旋转转换
                newShape[c][N - 1 - r] = shape[r][c];
            }
        }

        // 返回一个新的 Tetromino 对象，包含旋转后的形状，但类型和颜色不变
        return new Tetromino(this.type, newShape, this.color);
    }

    // Getter 方法

    public int[][] getShape() {
        return shape;
    }

    public Color getColor() {
        return color;
    }

    public int getType() {
        return type;
    }
}
