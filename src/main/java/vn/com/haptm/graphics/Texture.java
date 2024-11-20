package vn.com.haptm.graphics;

import org.lwjgl.system.MemoryStack;
import vn.com.haptm.util.ResourceManager;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.stb.STBImage.*;

public class Texture {
    private static int binded;
    public final int width;
    public final int height;
    private final int texture;

    public Texture(String resource) {
        final ByteBuffer buffer = ResourceManager.getBuffer(resource);
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final IntBuffer x = stack.mallocInt(1);
            final IntBuffer y = stack.mallocInt(1);
            final IntBuffer channels = stack.mallocInt(1);
            final ByteBuffer pixels = stbi_load_from_memory(buffer, x, y, channels, STBI_rgb_alpha);
            width = x.get(0);
            height = y.get(0);
            texture = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, texture);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glBindTexture(GL_TEXTURE_2D, 0);
            stbi_image_free(pixels);
        }
    }

    public static void unbind() {
        if (binded == 0)
            return;
        binded = 0;
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void bind() {
        if (binded == texture)
            return;
        binded = texture;
        glBindTexture(GL_TEXTURE_2D, texture);
    }

    public TextureRegion region(float x, float y, float width, float height) {
        final float s0 = x / this.width;
        final float t0 = y / this.height;
        final float s1 = (x + width) / this.width;
        final float t1 = (y + height) / this.height;
        return new TextureRegion(x, y, width, height, s0, t0, s1, t1);
    }
}


