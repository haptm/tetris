package vn.com.haptm.graphics;

public class TextureRegion {
    public final float x;
    public final float y;
    public final float width;
    public final float height;
    public final float s0;
    public final float t0;
    public final float s1;
    public final float t1;

    public TextureRegion(float x, float y, float width, float height, float s0, float t0, float s1, float t1) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.s0 = s0;
        this.t0 = t0;
        this.s1 = s1;
        this.t1 = t1;
    }
}
