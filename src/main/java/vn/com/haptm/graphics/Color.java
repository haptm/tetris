package vn.com.haptm.graphics;

public class Color {
    public float red = 1;
    public float green = 1;
    public float blue = 1;

    public void set(float red, float green, float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public void set(Color color) {
        red = color.red;
        green = color.green;
        blue = color.blue;
    }
}
