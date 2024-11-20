package vn.com.haptm.mat4f;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

public class Mat4f {
    public final FloatBuffer array = BufferUtils.createFloatBuffer(16);

    public Mat4f setOrtho(float left, float right, float bottom, float top) {
        array.put(0, 2 / (right - left));
        array.put(1, 0);
        array.put(2, 0);
        array.put(3, -(right + left) / (right - left));

        array.put(4, 0);
        array.put(5, 2 / (top - bottom));
        array.put(6, 0);
        array.put(7, -(top + bottom) / (top - bottom));

        array.put(8, 0);
        array.put(9, 0);
        array.put(10, 1);
        array.put(11, 0);

        array.put(12, 0);
        array.put(13, 0);
        array.put(14, 0);
        array.put(15, 1);
        return this;
    }
}
