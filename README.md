
# Tetris - Java Swing Implementation

![动画](https://github.com/user-attachments/assets/3ca18444-3ee5-4fbc-8e07-f3790bff9a8e)

## About / À Propos / 关于
This project is a classic Tetris game developed using Java and Swing. It is a personal project focused on blending mathematical logic with practical UI development.

Ce projet est un jeu de Tetris classique développé avec Java et Swing. Il s'agit d'un projet personnel axé sur la combinaison de la logique mathématique et du développement d'interfaces utilisateur.

这是一个使用 Java 和 Swing 开发的经典俄罗斯方块游戏。这是一个个人项目，重点在于将数学逻辑与实际的 UI 开发结合起来。

---

## Project Structure / Structure du Projet / 项目结构
```text
.
├── FrameTetris.java   # Main UI & Navigation (Supports HTML rendering)
├── GamePanel.java     # Core Engine (30+ methods: logic, scoring, UX delays)
└── Tetromino.java     # Data Model (Shapes & Linear Algebra rotation)

```

**Note:** All source code comments are written in Chinese.

**Note :** Tous les commentaires dans le code source sont rédigés en chinois.

**注意：** 所有源代码注释均使用中文编写。

---

## Technical Insights

The project is structured into three specialized classes to ensure clean separation of concerns:

* **FrameTetris**: Manages the window environment and navigation. During development, I discovered that Java Swing can parse HTML code for text rendering—a powerful feature I used for the interactive menus.
* **GamePanel**: The heart of the game featuring over 30 methods. It handles scoring, game state resets, and seamless menu transitions. I implemented a "Next Piece Preview" and a "Line Clear Delay" to enhance the player's experience.
* **Tetromino**: Defines the 7 classic shapes using 2D arrays.

### The Mathematical Approach (Rotation)

Instead of using conditional switches for each orientation, I implemented a Matrix Rotation algorithm. For a shape represented as an `N x N` matrix:

* **Logic**: A clockwise 90 degree rotation is achieved by transposing the matrix and then reversing each row.
* **Formula**:
`newShape[i][j] = oldShape[N - 1 - j][i]`

This linear algebra approach makes the code scalable for any potential new shape.

---

## Détails Techniques

Ce projet est une implémentation du Tetris classique en Java Swing, articulée autour de trois classes :

* **FrameTetris** : Gère l'interface principale et la navigation. J'ai appris lors du développement que Java Swing permet d'intégrer du code HTML pour le rendu du texte, ce qui m'a permis d'enrichir l'interface de manière inattendue.
* **GamePanel** : Le moteur du jeu contenant plus de 30 méthodes. Il gère le score, la réinitialisation, la prévisualisation de la prochaine pièce, et un système de délai d'effacement pour une fluidité optimale.
* **Tetromino** : Utilise des tableaux 2D pour les 7 formes.

### L'approche Mathématique (Rotation)

Au lieu d'utiliser des conditions pour chaque orientation, j'ai implémenté un algorithme de rotation matricielle. Pour une forme représentée par une matrice `N x N` :

* **Logique** : Une rotation de 90 degrés est obtenue en transposant la matrice, puis en inversant chaque ligne.
* **Formule** :
`newShape[i][j] = oldShape[N - 1 - j][i]`

Cette méthode rend le code évolutif pour n'importe quelle nouvelle forme.

---

## 技术细节

这是一个使用 Java + Swing 开发的俄罗斯方块项目。我在边学边做的过程中，将逻辑分成了三个核心模块：

* **FrameTetris (主界面类)**：负责窗口初始化和菜单切换。在开发中我偶然发现 Swing 竟然支持 HTML 代码，这让我能更灵活地处理界面文本。
* **GamePanel (游戏核心类)**：包含 30 多个方法，涵盖了计分、刷新、暂停及退出等功能。特别设计了“方块预览”和“延迟消除”功能，从玩家体验出发优化了游戏节奏。
* **Tetromino (方块逻辑类)**：通过二维数组定义了 7 种形状。

### 数学方法（旋转逻辑）

我没有为每个方向编写冗余的选择语句，而是使用了矩阵旋转算法。对于一个 `N x N` 的矩阵：

* **逻辑**：通过矩阵转置并翻转每一行，实现顺时针 90 度旋转。
* **公式**：
`newShape[i][j] = oldShape[N - 1 - j][i]`

这种线性代数方法使代码更具扩展性，能兼容以后可能加入的任何新形状。

<img width="1919" height="986" alt="image" src="https://github.com/user-attachments/assets/f8d1a3e6-e864-4891-a8ac-b3532c6649ea" />


