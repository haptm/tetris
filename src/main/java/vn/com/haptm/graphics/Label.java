package vn.com.haptm.graphics;

public class Label {
    public final Color color = new Color();
    public String text;
    public float x;
    public float y;
    public HAlign hAlign = HAlign.LEFT;
    public float hSpacing = .75f;
    public float vSpacing = .75f;
    public float size = 1;
    public float alpha = 1;

    public enum HAlign {
        LEFT, CENTER, RIGHT
    }
}
