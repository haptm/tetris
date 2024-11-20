package vn.com.haptm.graphics;

import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.opengl.GL30.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL30.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL30.GL_FLOAT;
import static org.lwjgl.opengl.GL30.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL30.GL_TRIANGLES;
import static org.lwjgl.opengl.GL30.GL_UNSIGNED_SHORT;
import static org.lwjgl.opengl.GL30.glBindBuffer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glBufferData;
import static org.lwjgl.opengl.GL30.glBufferSubData;
import static org.lwjgl.opengl.GL30.glDrawElements;
import static org.lwjgl.opengl.GL30.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glGenBuffers;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL30.glVertexAttribPointer;

public class Batch {
    private final int capacity;
    private int count;
    private FloatBuffer vertices;
    private int verticesSizePrev = -1;
    private ShortBuffer indices;
    private int indicesSizePrev = -1;
    private boolean drawing;
    private int vao;
    private int vbo;
    private int ibo;

    public Batch(int capacity) {
        this.capacity = capacity;
        vertices = MemoryUtil.memAllocFloat(capacity * 8);
        indices = MemoryUtil.memAllocShort(capacity * 6);

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices.capacity() * 4, GL_STREAM_DRAW);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(0, 4, GL_FLOAT, false, 32, 0);
        glVertexAttribPointer(1, 4, GL_FLOAT, false, 32, 16);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        ibo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices.capacity() * 2, GL_STREAM_DRAW);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        glBindVertexArray(0);
    }

    private void addIndices() {
        final int newSize = indices.position() + 6;
        if (newSize > indices.capacity()) {
            System.err.println("index array of buffer overflow " + newSize + " > " + indices.capacity());
            return;
        }

        final int verticesCount = vertices.position() / 8;
        indices.put((short) verticesCount);
        indices.put((short) (verticesCount + 1));
        indices.put((short) (verticesCount + 2));
        indices.put((short) (verticesCount + 2));
        indices.put((short) (verticesCount + 1));
        indices.put((short) (verticesCount + 3));
    }

    private void addSpriteVertex(Sprite sprite, float w, float h) {
        final int newSize = vertices.position() + 8;
        if (newSize > vertices.capacity()) {
            System.err.println("vertex array of buffer overflow " + newSize + " > " + vertices.capacity());
            return;
        }

        vertices.put(sprite.x + w * sprite.width);
        vertices.put(sprite.y + h * sprite.height);
        vertices.put(w * sprite.textureRegion.s1 + (1 - w) * sprite.textureRegion.s0);
        vertices.put(h * sprite.textureRegion.t0 + (1 - h) * sprite.textureRegion.t1);
        vertices.put(sprite.color.red);
        vertices.put(sprite.color.green);
        vertices.put(sprite.color.blue);
        vertices.put(sprite.alpha);
    }

    public void draw(Sprite sprite) {
        final int newCount = count + 1;
        if (newCount > capacity) {
            System.err.println("sprite overflow " + newCount + " > " + capacity);
            return;
        }

        addIndices();
        addSpriteVertex(sprite, 0, 1);
        addSpriteVertex(sprite, 0, 0);
        addSpriteVertex(sprite, 1, 1);
        addSpriteVertex(sprite, 1, 0);
        count = newCount;
    }

    private void addLabelVertex(Label label, float x, float y, float w, float h, float s, float t) {
        final int newSize = vertices.position() + 8;
        if (newSize > vertices.capacity()) {
            System.err.println("vertex array of buffer overflow " + newSize + " > " + vertices.capacity());
            return;
        }

        vertices.put(label.x + x + w * label.size);
        vertices.put(label.y + y + h * label.size);
        vertices.put(s);
        vertices.put(t);
        vertices.put(label.color.red);
        vertices.put(label.color.green);
        vertices.put(label.color.blue);
        vertices.put(label.alpha);
    }

    private void drawChar(Label label, char c, float x, float y) {
        final int index = c - 32;
        final int s = index % 16;
        final int t = index / 16;
        final float s0 = s / 16f;
        final float t0 = t / 16f;
        final float s1 = (s + 1) / 16f;
        final float t1 = (t + 1) / 16f;
        addIndices();
        addLabelVertex(label, x, y, 0, 1, s0, t0);
        addLabelVertex(label, x, y, 0, 0, s0, t1);
        addLabelVertex(label, x, y, 1, 1, s1, t0);
        addLabelVertex(label, x, y, 1, 0, s1, t1);
    }

    private float getLabelWidth(Label label) {
        int w = 0;
        int x = 0;
        for (int i = 0; i < label.text.length(); i++) {
            final char c = label.text.charAt(i);
            if (c == '\n') {
                w = Math.max(w, x);
                x = 0;
                continue;
            }
            x++;
        }
        w = Math.max(w, x);
        return label.size * (label.hSpacing * (w - 1) + 1);
    }

    public void draw(Label label) {
        final float w;
        switch (label.hAlign) {
            default:
                w = 0;
                break;
            case CENTER:
                w = (this.getLabelWidth(label)) / 2;
                break;
            case RIGHT:
                w = this.getLabelWidth(label);
                break;
        }
        float x = 0;
        float y = 0;
        for (int i = 0; i < label.text.length(); i++) {
            final char c = label.text.charAt(i);
            if (c == '\n') {
                x = 0;
                y -= label.size * label.vSpacing;
                continue;
            }
            this.drawChar(label, c, x - w, y);
            x += label.size * label.hSpacing;
        }
    }

    public void begin() {
        if (drawing)
            return;
        drawing = true;

        count = 0;
        vertices.clear();
        indices.clear();
    }

    public void end() {
        if (!this.drawing)
            return;
        drawing = false;

        vertices.flip();
        indices.flip();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);

        if (vertices.limit() == verticesSizePrev)
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
        else
            glBufferData(GL_ARRAY_BUFFER, vertices, GL_STREAM_DRAW);

        if (indices.limit() == indicesSizePrev)
            glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0, indices);
        else
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STREAM_DRAW);

        glDrawElements(GL_TRIANGLES, indices.limit(), GL_UNSIGNED_SHORT, 0);

        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        verticesSizePrev = vertices.limit();
        indicesSizePrev = indices.limit();
    }
}
