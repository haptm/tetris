package vn.com.haptm.game;

public class Shape {
    public int size;
    public boolean[][] cells;
    public float centerX;
    public float centerY;

    public Shape() {
        this(0, new boolean[0][0]);
    }

    public Shape(int size, boolean[][] cells) {
        this.size = size;
        this.cells = cells;
        calcCenter();
    }

    public void reset() {
        size = 0;
        cells = new boolean[0][0];
        centerX = 0;
        centerY = 0;
    }

    private void calcCenter() {
        int xMin = size - 1;
        int yMin = size - 1;
        int xMax = 0;
        int yMax = 0;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (!hasCell(x, y))
                    continue;
                xMin = Math.min(xMin, x);
                yMin = Math.min(yMin, y);
                xMax = Math.max(xMax, x + 1);
                yMax = Math.max(yMax, y + 1);
            }
        }
        centerX = (xMin + xMax) / 2f;
        centerY = (yMin + yMax) / 2f;
    }

    // sao chép shape khác vào đối tượng hiện tại
    public void set(Shape shape) {
        size = shape.size;
        this.cells = new boolean[shape.size][shape.size];
        for (int y = 0; y < size; y++) {
            System.arraycopy(shape.cells[y], 0, this.cells[y], 0, size);
        }
        calcCenter();
    }

    public boolean hasCell(int x, int y) {
        if (x < 0 || x >= size || y < 0 || y >= size)
            return false;
        return cells[y][x];
    }

    public void rotate(int direction) {
        if (direction < 0)
            rotateCCW();
        else if (direction > 0)
            rotateCW();
    }

    // xoay theo chiều kim đồng hồ
    private void rotateCW() {
        boolean[][] newCells = new boolean[size][size];

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (!hasCell(x, y))
                    continue;
                final int newY = size - 1 - x;
                final int newX = y;
                newCells[newY][newX] = true;
            }
        }
        this.cells = newCells;
        calcCenter();
    }

    // Xoay ngược chiều kim đồng hồ
    private void rotateCCW() {
        boolean[][] newCells = new boolean[size][size];

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (!hasCell(x, y))
                    continue;
                final int newY = x;
                final int newX = size - 1 - y;
                newCells[newY][newX] = true;
            }
        }
        this.cells = newCells;
        calcCenter();
    }
}
