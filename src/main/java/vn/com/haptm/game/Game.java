package vn.com.haptm.game;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Game {
    private static final Shape[] SHAPES;

    static {
        SHAPES = new Shape[]{
                // S
                new Shape(3, new boolean[][] {
                        {false, true, true},
                        {true, true, false},
                        {false, false, false}
                }),
                // Z
                new Shape(3, new boolean[][] {
                        {true, true, false},
                        {false, true, true},
                        {false, false, false}
                }),
                // I
                new Shape(4, new boolean[][] {
                        {false, false, false, false},
                        {true, true, true, true},
                        {false, false, false, false},
                        {false, false, false, false}
                }),
                // L
                new Shape(3, new boolean[][] {
                        {false, false, true},
                        {true, true, true},
                        {false, false, false}
                }),
                // J
                new Shape(3, new boolean[][] {
                        {true, false, false},
                        {true, true, true},
                        {false, false, false}
                }),
                //T
                new Shape(3, new boolean[][] {
                        {false, true, false},
                        {true, true, true},
                        {false, false, false}
                }),
                // O
                new Shape(2, new boolean[][] {
                        {true, true},
                        {true, true}
                })
        };
    }

    public final In in;
    public final Board board;
    public final Player player;
    public final Player shadow;
    public final Shape hold;
    public final Shape tempShape;
    public int level;
    public int count;
    public float time;
    public int score;
    // thời gian trễ giữa các lần rơi
    public float fallCD;
    // thời gian trễ khi xoá các hàng
    public float clearCD;
    // thời gian khi hàng bị xoá
    public float clearTime;
    public int clearY0;
    public int clearY1;
    public Shape next;
    public boolean canHold;
    public boolean gameOver;
    private int highScore;

    private int readHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/highScore.txt"))) {
            String line = reader.readLine();
            if (line != null) {
                return Integer.parseInt(line.trim());
            }
        } catch (IOException | NumberFormatException e) {
            return 0;
        }
        return 0;
    }

    private void writeHighScore() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/highScore.txt"))) {
            writer.write(String.valueOf(this.highScore));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateHighScore() {
        if (this.score > this.highScore) {
            this.highScore = this.score;
            writeHighScore();
        }
    }

    public Game(Config config) {
        in = new In();
        board = new Board(config.board);
        player = new Player();
        shadow = new Player();
        hold = new Shape();
        tempShape = new Shape();
        highScore = readHighScore();
        reset();
    }

    private static Shape randomShape() {
        return SHAPES[(int) (Math.random() * SHAPES.length)];
    }

    public void reset() {
        in.reset();
        level = 1;
        count = 0;
        time = 0;
        score = 0;
        fallCD = 0;
        clearCD = 0;
        clearTime = 0;
        clearY0 = 0;
        clearY1 = 0;
        board.reset();
        player.reset();
        shadow.reset();
        next = randomShape();
        canHold = false;
        hold.reset();
        tempShape.reset();
        gameOver = false;
    }

    // tính toán thời gian giữa các lần rơi
    private float nextFallCD() {
        return 0.1f + 0.9f * (99 - level) / 98;
    }

    // tính toán bóng của player
    private void calcShadow() {
        shadow.set(player);
        do {
            shadow.y--;
        } while (board.checkPlayer(shadow));
        // loop sẽ di chuyển shadow đến cái hàng đang có shape khác nên cần cộng lên 1 để đúng vị trí
        shadow.y++;
    }

    // đặt shape vào vị trí của player
    private void setPlayer(Shape shape) {
        updateHighScore();
        player.set(shape);
        player.x = (board.width - player.size) / 2;
        player.y = (board.height - player.size) / 2;
        do {
            player.y++;
        } while (board.checkPlayer(player, true));
        player.y--;
        calcShadow();
    }

    // đặt player lên bảng khi nó rơi xuống
    private void placePlayer() {
        if (player.size > 0) {
            final int rows = board.placePlayer(player);
            score += rows * 10;
            if (score >= 100) level++;
            if (rows > 0) {
                clearTime = this.time;
                clearCD = .4f;
                clearY0 = this.player.y;
                clearY1 = this.player.y + this.player.size - 1;
                player.reset();
                shadow.reset();
                return;
            }
        }

        setPlayer(next);
        next = randomShape();
        canHold = true;
    }

    private void setGameOver() {
        gameOver = true;
        player.reset();
        shadow.reset();
    }

    public void update(float delta) {
        if (gameOver)
            return;
        time += delta;
        if (clearCD == 0) {
            updateInMoveX();
            updateInRotate();
            updateInFall();
            updateInDrop();
            updateInHold();
            updateFall(delta);
        } else {
            updateClear(delta);
        }
        in.reset();
    }

    private void updateInMoveX() {
        if (in.moveX != 0) {
            player.x += (int) in.moveX;
            if (board.checkPlayer(player)) {
                calcShadow();
                return;
            }
            player.x -= (int) in.moveX;
        }
    }

    private void updateInRotate() {
        if (in.rotate != 0) {
            player.rotate(in.rotate);
            if (board.checkPlayer(player)) {
                calcShadow();
                return;
            }
            player.rotate(-in.rotate);
        }
    }

    private void updateInFall() {
        if (in.fall) {
            fallCD = 0;
        }
    }

    private void updateInDrop() {
        if (in.drop) {
            fallCD = 0;
            player.set(shadow);
        }
    }

    private void updateInHold() {
        if (in.hold) {
            if (!canHold)
                return;
            canHold = false;
            if (hold.size > 0)
                tempShape.set(hold);
            hold.set(player);
            if (tempShape.size == 0) {
                setPlayer(next);
                next = randomShape();
            } else
                setPlayer(tempShape);
            tempShape.reset();
        }
    }

    private void updateFall(float delta) {
        fallCD -= delta;
        if (fallCD <= 0) {
            fallCD += nextFallCD();
            if (player.size > 0) {
                player.y--;
                if (board.checkPlayer(player))
                    return;
                player.y++;
            }
            placePlayer();
            if (board.checkPlayer(player))
                return;
            placePlayer();
            setGameOver();
        }
    }

    private void updateClear(float delta) {
        this.clearCD -= delta;
        if (clearCD <= 0) {
            clearCD = 0;
            for (int y = clearY1; y >= clearY0; y--)
                if (board.isFullRow(y))
                    board.removeRow(y);
            setPlayer(next);
            next = randomShape();
            canHold = true;
        }
    }

    public static class In {
        public float moveX;
        public int rotate;
        public boolean fall;
        public boolean drop;
        public boolean hold;

        public void reset() {
            moveX = 0;
            rotate = 0;
            fall = false;
            drop = false;
            hold = false;
        }
    }

    public static class Config {
        public Board.Config board = new Board.Config();
    }
}
