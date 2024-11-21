package vn.com.haptm.game;

import java.util.Arrays;

public class Board {
    public final int width;
    public final int height;
    public final boolean[][] cells;

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        cells = new boolean[this.height][this.width];
    }

    public void reset() {
        for (int y = 0; y < height; y++) {
            Arrays.fill(cells[y], false);
        }
    }

    public void setCell(int x, int y, boolean cell) {
        if (!isOutOfBounds(x, y)) {
            cells[y][x] = cell;
        }
    }

    public boolean isFullRow(int y) {
        if (y < 0 || y >= height) return false;
        for (boolean cell : cells[y]) {
            if (!cell) return false;
        }
        return true;
    }

    public void removeRow(int y) {
        if (y >= 0 && y < height) {
            // Di chuyển tất cả các hàng phía trên y xuống dưới một vị trí
            for (int i = y; i < height - 1; i++) {
                System.arraycopy(cells[i + 1], 0, cells[i], 0, width);
            }
            // Reset hàng cuối cùng (hàng cao nhất sau khi di chuyển)
            Arrays.fill(cells[height - 1], false);
        }
    }

    public boolean hasCell(int x, int y) {
        return !isOutOfBounds(x, y) && cells[y][x];
    }

    public boolean isOutOfBounds(int x, int y) {
        return x < 0 || x >= width || y < 0 || y >= height;
    }

    public boolean checkPlayer(Player player) {
        return checkPlayer(player, false);
    }

    // Kiểm tra khối có hợp lệ tại vị trí hiện tại ko
    public boolean checkPlayer(Player player, boolean ignoreRows) {
        for (int y = 0; y < player.size; y++) {
            for (int x = 0; x < player.size; x++) {
                if (!player.hasCell(x, y))
                    continue;
                final int bx = player.x + x;
                final int by = player.y + y;
                if (isOutOfBounds(bx, by))
                    return false;
                if (!ignoreRows)
                    if (hasCell(bx, by))
                        return false;
            }
        }
        return true;
    }

    // Đặt khối lên bảng, tính toán số hàng đầy
    public int placePlayer(Player player) {
        int rows = 0;
        for (int y = 0; y < player.size; y++) {
            final int by = player.y + y;
            for (int x = 0; x < player.size; x++) {
                if (!player.hasCell(x, y))
                    continue;
                final int bx = player.x + x;
                if (isOutOfBounds(bx, by))
                    continue;
                setCell(bx, by, true);
            }

            if (isFullRow(by))
                rows++;
        }
        return rows;
    }
}

