package vn.com.haptm;

import vn.com.haptm.game.Game;
import vn.com.haptm.game.Player;
import vn.com.haptm.game.Shape;
import vn.com.haptm.graphics.Batch;
import vn.com.haptm.graphics.Graphics;
import vn.com.haptm.graphics.Label;
import vn.com.haptm.graphics.Shader;
import vn.com.haptm.graphics.Sprite;
import vn.com.haptm.graphics.Texture;
import vn.com.haptm.inputs.Inputs;
import vn.com.haptm.mat4f.Mat4f;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_DOWN;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_P;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UP;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_X;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Z;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class TetrisGame {
    private static final int BOARD_HEIGHT = 20;
    private static final int BOARD_WIDTH = 10;
    // Ma trận chiếu
    private static final Mat4f projection = new Mat4f();
    private static Texture texture;
    private static Game tetris;
    private static Sprites sprites;
    private static Labels labels;
    // render theo batch
    private static Batch batch;
    private static boolean paused;

    public static String getHighScore() {
        // Đường dẫn đến tệp highScore.txt trong thư mục người dùng
        String filePath = System.getProperty("user.home") + "/highScore.txt";
        String highScore = "0"; // Giá trị mặc định nếu không đọc được tệp

       // Kiểm tra nếu tệp không tồn tại, tạo mới
        File file = new File(filePath);

        // Đọc nội dung từ tệp
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            highScore = Objects.requireNonNullElse(line, "0"); // Trả về "0" nếu không có nội dung
        } catch (IOException e) {
            // Xử lý lỗi nếu có sự cố khi đọc tệp
            System.err.println("IOException occurred: " + e.getMessage());
            e.printStackTrace();
        }

        return highScore; // Trả về điểm cao đã đọc được
    }



    public static String formatTime(double t) {
        var s = (int) t;
        var m = (int) (s / 60f);
        s %= 60;
        return String.format("%01d:%02d", m, s);
    }

    private static void initialize() {
        Inputs.initialize();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_CULL_FACE);
        glClearColor(0f, 0.0f, 0.0f, 1.0f);
        glActiveTexture(GL_TEXTURE0);
        glViewport(0, 0, 600, 800);

        projection.setOrtho(0, 15, 0, 20);

        Shader shader = new Shader();
        shader.bind();
        shader.setUniformMat4f("u_projection", projection);
        shader.setUniform1i("u_material", 0);

        texture = new Texture("textures/spritesheet.png");
        texture.bind();

        tetris = new Game();

        sprites = new Sprites();
        labels = new Labels();

        batch = new Batch(2048);
    }

    // màu ô trong nền
    private static Sprite createBoardPieceSprite() {
        final Sprite sprite = new Sprite();
        sprite.textureRegion = texture.region(0, 64, 1, 1);
        sprite.color.set(1, 1, 1);
        return sprite;
    }

    // màu nền hold next
    private static Sprite createHNSprite() {
        final Sprite sprite = new Sprite();
        sprite.textureRegion = texture.region(0, 64, 1, 1);
        sprite.color.set(.9f, .95f, .9f);
        return sprite;
    }

    // màu nền khi hold có khối mới
    private static Sprite createHoldNewSprite() {
        final Sprite sprite = new Sprite();
        sprite.textureRegion = texture.region(0, 64, 1, 1);
        sprite.color.set(1,1,1);
        sprite.alpha = 0.75f;
        return sprite;
    }

    // nền game over
    private static Sprite createGameOverSprite() {
        final Sprite sprite = new Sprite();
        sprite.textureRegion = texture.region(0, 64, 1, 1);
        sprite.color.set(.08f, .10f, .08f);
        sprite.alpha = .96f;
        return sprite;
    }

    // màu nền board
    private static Sprite createBoardBGPieceSprite() {
        final Sprite sprite = new Sprite();
        sprite.textureRegion = texture.region(0, 64, 8, 8);
        sprite.color.set(.975f, .96f, .975f);
        return sprite;
    }

    // shape trong next và hold
    private static Sprite createBoardHNPieceSprite() {
        final Sprite sprite = new Sprite();
        sprite.textureRegion = texture.region(0, 64, 8, 8);
        sprite.color.set(0, .1f, 0);
        return sprite;
    }

    private static Sprite createClearPieceSprite() {
        final Sprite sprite = new Sprite();
        sprite.textureRegion = texture.region(0, 64, 8, 8);
        return sprite;
    }

    private static Sprite createPlayerPieceSprite() {
        final Sprite sprite = new Sprite();
        sprite.textureRegion = texture.region(0, 64, 8, 8);
        return sprite;
    }

    private static Sprite createShadowPieceSprite() {
        final Sprite sprite = new Sprite();
        sprite.textureRegion = texture.region(0, 64, 8, 8);
        return sprite;
    }

    private static Label createTimeLabel() {
        final Label label = new Label();
        label.text = "TIME";
        label.x = 2f;
        label.y = BOARD_HEIGHT - 0.8f;
        label.size = .5f;
        label.hAlign = Label.HAlign.RIGHT;
        label.color.set(1f, 1f, 1f);
        return label;
    }

    private static Label createTimeValueLabel() {
        final Label label = new Label();
        label.x = 4.5f;
        label.y = BOARD_HEIGHT - 1.5f;
        label.size = .5f;
        label.hAlign = Label.HAlign.RIGHT;
        label.color.set(1f, 1f, 1f);
        return label;
    }

    private static Label createScoreLabel() {
        final Label label = new Label();
        label.text = "SCORE";
        label.x = 2.4f;
        label.y = BOARD_HEIGHT - 2.8f;
        label.size = .5f;
        label.hAlign = Label.HAlign.RIGHT;
        label.color.set(1f, 1f, 1f);
        return label;
    }

    private static Label createScoreValueLabel() {
        final Label label = new Label();
        label.x = 4.5f;
        label.y = BOARD_HEIGHT - 3.5f;
        label.hAlign = Label.HAlign.RIGHT;
        label.size = .5f;
        label.color.set(1f, 1f, 1f);
        return label;
    }

    private static Label createLevelLabel() {
        final Label label = new Label();
        label.text = "LEVEL";
        label.x = 2.4f;
        label.y = BOARD_HEIGHT - 4.8f;
        label.size = .5f;
        label.hAlign = Label.HAlign.RIGHT;
        label.color.set(1f, 1f, 1f);
        return label;
    }

    private static Label createLevelValueLabel() {
        final Label label = new Label();
        label.x = 4.5f;
        label.y = BOARD_HEIGHT - 5.5f;
        label.hAlign = Label.HAlign.RIGHT;
        label.size = .5f;
        label.color.set(1f, 1f, 1f);
        return label;
    }

    private static Label createHighScoreLabel() {
        final Label label = new Label();
        label.text = "HIGH SCORE";
        label.x = 4.3f;
        label.y = BOARD_HEIGHT - 6.8f;
        label.size = .5f;
        label.hAlign = Label.HAlign.RIGHT;
        label.color.set(1f, 1f, 1f);
        return label;
    }

    private static Label createHighScoreValueLabel() {
        final Label label = new Label();
        label.x = 4.5f;
        label.y = BOARD_HEIGHT - 7.5f;
        label.hAlign = Label.HAlign.RIGHT;
        label.size = .5f;
        label.color.set(1f, 1f, 1f);
        return label;
    }

    private static Label createNextLabel() {
        final Label label = new Label();
        label.text = "NEXT";
        label.x = 0.4f;
        label.y = 5.25f;
        label.size = .65f;
        label.hAlign = Label.HAlign.LEFT;
        label.color.set(1f, 1f, 1f);
        return label;
    }

    private static Label createHoldLabel() {
        final Label label = new Label();
        label.text = "HOLD";
        label.x = 0.4f;
        label.y = 11.5f;
        label.size = .65f;
        label.hAlign = Label.HAlign.LEFT;
        label.color.set(1f, 1f, 1f);
        return label;
    }

    private static Label createGameOverLabel() {
        final Label label = new Label();
        label.text = "GAME\nOVER";
        label.x = 5 + (float) BOARD_WIDTH / 2;
        label.y = 1 + (float) BOARD_HEIGHT / 2;
        label.vSpacing = 1;
        label.size = 3;
        label.hAlign = Label.HAlign.CENTER;
        label.color.set(1f, 1f, 1f);
        return label;
    }

    public static void update(float delta) {
        var reset = false;
        var dx = 0;
        var rotate = 0;
        var fall = false;
        var drop = false;
        var hold = false;

        Inputs.poolEvents(delta);
        if (Inputs.isDown(GLFW_KEY_P, 1, 1)) paused = !paused;
        if (Inputs.isDown(GLFW_KEY_R, 1, 1)) reset = true;
        if (Inputs.isDown(GLFW_KEY_LEFT, 0.16f, 0.04f)) dx--;
        if (Inputs.isDown(GLFW_KEY_RIGHT, 0.16f, 0.04f)) dx++;
        if (Inputs.isDown(GLFW_KEY_DOWN, 0.16f, 0.04f)) fall = true;
        if (Inputs.isDown(GLFW_KEY_SPACE, 1, 1)) drop = true;
        if (Inputs.isDown(GLFW_KEY_UP, 1, 1)) hold = true;
        if (Inputs.isDown(GLFW_KEY_X, 0.16f, 0.04f)) rotate = 1;
        if (Inputs.isDown(GLFW_KEY_Z, 0.16f, 0.04f)) rotate = -1;

        if (tetris.gameOver)
            paused = false;

        if (reset)
            tetris.reset();
        if (!paused) {
            tetris.in.moveX = dx;
            tetris.in.rotate = rotate;
            tetris.in.fall = fall;
            tetris.in.drop = drop;
            tetris.in.hold = hold;
            tetris.update(delta);
        }
    }

    public static void renderBoard() {
        renderBG(sprites.boardPiece, 5, 0, tetris.board.width, tetris.board.height);
        for (var y = 0; y < tetris.board.height; y++) {
            var isFullRow = tetris.board.isFullRow(y);
            for (var x = 0; x < tetris.board.width; x++) {
                Sprite spt;
                if (paused)
                    spt = sprites.pieces.boardFG;
                else if (isFullRow)
                    spt = sprites.pieces.clear;
                else if (tetris.board.hasCell(x, y))
                    spt = sprites.pieces.boardFG;
                else
                    spt = sprites.pieces.boardBG;
                spt.x = x + 5;
                spt.y = y;
                batch.draw(spt);
            }
        }
    }

    public static void renderShape(Shape shape, Sprite spt, float wx, float wy) {
        if (shape instanceof Player) {
            wx += ((Player) shape).x;
            wy += ((Player) shape).y;
        }
        for (var y = 0; y < shape.size; y++)
            for (var x = 0; x < shape.size; x++) {
                if (shape.hasCell(x, y)) {
                    spt.x = wx + x;
                    spt.y = wy + y;
                    batch.draw(spt);
                }
            }
    }

    public static void renderShapeBox(Shape shape, Sprite spt, float x, float y) {
        renderBG(sprites.holdNextBG, x, y, 4, 4);
        if (shape.size > 0) {
            var wx = x + 2 - shape.centerX;
            var wy = y + 2 - shape.centerY;
            renderShape(shape, spt, wx, wy);
        }
    }

    public static void renderBG(Sprite spt, float x, float y, float w, float h) {
        spt.x = x;
        spt.y = y;
        spt.width = w;
        spt.height = h;
        batch.draw(spt);
    }

    public static void renderTime() {
        labels.timeValue.text = formatTime(tetris.time);
        batch.draw(labels.time);
        batch.draw(labels.timeValue);
    }

    public static void renderScore() {
        labels.scoreValue.text = String.valueOf(tetris.score);
        batch.draw(labels.score);
        batch.draw(labels.scoreValue);
    }

    public static void renderLevel() {
        labels.levelValue.text = String.valueOf(tetris.level);
        batch.draw(labels.level);
        batch.draw(labels.levelValue);
    }

    public static void renderHighScore() {
        labels.highScoreValue.text = getHighScore();
        batch.draw(labels.highScore);
        batch.draw(labels.highScoreValue);
    }

    public static void renderNext() {
        batch.draw(labels.next);
    }

    public static void renderHold() {
        batch.draw(labels.hold);
    }

    public static void renderGameOver() {
        batch.draw(labels.gameOver);
    }

    public static void render(double time) {
        sprites.pieces.player.color.set(0, 0, 1);
        sprites.pieces.shadow.color.set(0, 0, 1);
        sprites.pieces.shadow.alpha = .5f;

        glClear(GL_COLOR_BUFFER_BIT);
        batch.begin();

        renderBoard();
        if (!paused) {
            renderShape(tetris.shadow, sprites.pieces.shadow, 5, 0);
            renderShape(tetris.player, sprites.pieces.player, 5, 0);
            renderShapeBox(tetris.next, sprites.pieces.boardFG, 0.5f, 1);
            renderShapeBox(tetris.hold, sprites.pieces.boardFG, 0.5f, 7);
            if (!tetris.canHold)
                renderBG(sprites.holdNewBG, 0.5f, 7, 4, 4);
        }
        if (tetris.gameOver) {
            renderBG(sprites.gameOverBG, 5, 0, tetris.board.width, tetris.board.height);
            renderBG(sprites.gameOverBG, 0, 1, 4, 4);
            renderBG(sprites.gameOverBG, 0, 7, 4, 4);
            renderGameOver();
        }
        renderTime();
        renderScore();
        renderLevel();
        renderNext();
        renderHold();
        renderHighScore();

        batch.end();
    }

    public static void main(String[] args) {
        Graphics.initialize(new Graphics.Config() {
            {
                width = 600;
                height = 800;
                initializeCallback = TetrisGame::initialize;
                updateCallback = TetrisGame::update;
                renderCallback = TetrisGame::render;
            }
        });
    }

    private static final class Sprites {
        public Sprite boardPiece = createBoardPieceSprite();
        public Sprite holdNextBG = createHNSprite();
        public Sprite holdNewBG = createHoldNewSprite();
        public Sprite gameOverBG = createGameOverSprite();
        public Pieces pieces = new Pieces();

        public static final class Pieces {
            public Sprite boardBG = createBoardBGPieceSprite();
            public Sprite boardFG = createBoardHNPieceSprite();
            public Sprite clear = createClearPieceSprite();
            public Sprite player = createPlayerPieceSprite();
            public Sprite shadow = createShadowPieceSprite();
        }
    }

    private static final class Labels {
        public Label time = createTimeLabel();
        public Label timeValue = createTimeValueLabel();
        public Label score = createScoreLabel();
        public Label scoreValue = createScoreValueLabel();
        public Label level = createLevelLabel();
        public Label levelValue = createLevelValueLabel();
        public Label next = createNextLabel();
        public Label hold = createHoldLabel();
        public Label gameOver = createGameOverLabel();
        public Label highScore = createHighScoreLabel();
        public Label highScoreValue = createHighScoreValueLabel();
    }
}
