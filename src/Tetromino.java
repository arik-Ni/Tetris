import java.awt.Color;
import java.util.Random;

/*
 * Tetromino 类：这块管的就是方块长什么样，怎么转 | Définition des formes, couleurs et rotations
 * 用 2D 数组存坐标，1 是有肉，0 是空的 | Grille 2D : 1 pour le bloc, 0 pour le vide
 */
public class Tetromino {
    // 七种形状的 ID | Types standard des 7 pièces
    public static final int TYPE_I = 1;
    public static final int TYPE_J = 2;
    public static final int TYPE_L = 3;
    public static final int TYPE_O = 4;
    public static final int TYPE_S = 5;
    public static final int TYPE_T = 6;
    public static final int TYPE_Z = 7;

    private int type;
    private int[][] shape;
    private Color color;

    // 内部构造，外面统一调工厂方法 | Constructeur privé, passage par factory
    private Tetromino(int type, int[][] shape, Color color) {
        this.type = type;
        this.shape = shape;
        this.color = color;
    }

    // --- 工厂方法区：各种形状的初始矩阵 | Factory : Formes initiales ---

    public static Tetromino createI() {
        // I 型：唯一一个用 4x4 的，转起来才不歪 | La barre (I) en 4x4 pour la rotation
        int[][] iShape = new int[][] {
                {0, 0, 0, 0},
                {1, 1, 1, 1},
                {0, 0, 0, 0},
                {0, 0, 0, 0}
        };
        return new Tetromino(TYPE_I, iShape, Color.CYAN);
    }

    public static Tetromino createJ() {
        // J 型：蓝色 | J en bleu
        int[][] jShape = new int[][] {
                {1, 0, 0},
                {1, 1, 1},
                {0, 0, 0}
        };
        return new Tetromino(TYPE_J, jShape, Color.BLUE);
    }

    public static Tetromino createL() {
        // L 型：橙色 | L en orange
        int[][] lShape = new int[][] {
                {0, 0, 1},
                {1, 1, 1},
                {0, 0, 0}
        };
        return new Tetromino(TYPE_L, lShape, Color.ORANGE);
    }

    public static Tetromino createO() {
        // O 型：2x2 够用了，反正转不转都一样 | Carré (O), rotation inutile mais bon...
        int[][] oShape = new int[][] {
                {1, 1},
                {1, 1}
        };
        return new Tetromino(TYPE_O, oShape, Color.YELLOW);
    }

    public static Tetromino createS() {
        // S 型：绿色 | S en vert
        int[][] sShape = new int[][] {
                {0, 1, 1},
                {1, 1, 0},
                {0, 0, 0}
        };
        return new Tetromino(TYPE_S, sShape, Color.GREEN);
    }

    public static Tetromino createT() {
        // T 型：洋红 | T en magenta
        int[][] tShape = new int[][] {
                {0, 1, 0},
                {1, 1, 1},
                {0, 0, 0}
        };
        return new Tetromino(TYPE_T, tShape, Color.MAGENTA);
    }

    public static Tetromino createZ() {
        // Z 型：红色 | Z en rouge
        int[][] zShape = new int[][] {
                {1, 1, 0},
                {0, 1, 1},
                {0, 0, 0}
        };
        return new Tetromino(TYPE_Z, zShape, Color.RED);
    }

    // 随机抽奖，给哪个是哪个 | Randomisation des pièces
    private static final Random random = new Random();

    public static Tetromino createRandom() {
        int randomType = random.nextInt(7) + 1; // 1 到 7 随机

        switch (randomType) {
            case TYPE_I: return createI();
            case TYPE_J: return createJ();
            case TYPE_L: return createL();
            case TYPE_O: return createO();
            case TYPE_S: return createS();
            case TYPE_Z: return createZ();
            default: return createT(); // 万一出鬼了，默认给个 T 型保底
        }
    }

    // --- 旋转核心逻辑 | Logique de rotation ---

    /**
     顺时针转 90 度，逻辑就是坐标换位 | Rotation horaire à 90 degrés
     注意：转完得回 GamePanel 查查有没有撞墙，这儿只管转 | On tourne l'objet, à vérifier après
     */
    public Tetromino rotate() {
        // 矩阵转换：newShape[c][N - 1 - r] = shape[r][c]
        // 这是最稳的顺时针算法 | Algorithme de rotation de matrice standard

        int N = shape.length;
        int[][] newShape = new int[N][N];

        for (int r = 0; r < N; r++) {
            for (int c = 0; c < N; c++) {
                newShape[c][N - 1 - r] = shape[r][c];
            }
        }

        // 搞个新对象甩回去，别动老的 | On renvoie un nouvel objet
        return new Tetromino(this.type, newShape, this.color);
    }



    // 普通的 Getter | Accesseurs de base

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