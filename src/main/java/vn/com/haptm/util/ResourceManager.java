package vn.com.haptm.util;

import org.lwjgl.BufferUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public final class ResourceManager {
    private ResourceManager() {
    }

    public static InputStream open(String resource) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
    }

    public static ByteBuffer getBuffer(String resource) {
        try (final BufferedInputStream stream = new BufferedInputStream(open(resource))) {
            final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            final byte[] buffer = new byte[8192];
            int len;
            while ((len = stream.read(buffer)) > 0)
                bytes.write(buffer, 0, len);
            final ByteBuffer bb = BufferUtils.createByteBuffer(bytes.size());
            bb.put(bytes.toByteArray()).flip();
            return bb;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
