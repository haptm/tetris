package vn.com.haptm.game;

public class Player extends Shape {
    public int x;
    public int y;

    public Player() {
        super();
        x = 0;
        y = 0;
    }

    @Override
    public void reset() {
        super.reset();
        x = 0;
        y = 0;
    }

    @Override
    public void set(Shape shape) {
        super.set(shape);
        if (shape instanceof Player) {
            x = ((Player) shape).x;
            y = ((Player) shape).y;
        }
    }
}
