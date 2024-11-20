package vn.com.haptm.graphics;

import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.glfw.GLFW.glfwGetVersionString;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwHideWindow;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_VENDOR;
import static org.lwjgl.opengl.GL11.GL_VERSION;
import static org.lwjgl.opengl.GL11.glGetString;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL20.GL_SHADING_LANGUAGE_VERSION;
import static org.lwjgl.system.MemoryUtil.NULL;

public final class Graphics {
    public static final Surface surface = new Surface();
    public static final Viewport viewport = new Viewport();
    public static long window;
    private static float ratio;
    private static InitializeCallback initializeCallback;
    private static UpdateCallback updateCallback;
    private static RenderCallback renderCallback;

    private Graphics() {
    }

    public static void initialize(Config config) {
        ratio = (float) config.width / config.height;
        initializeCallback = config.initializeCallback;
        updateCallback = config.updateCallback;
        renderCallback = config.renderCallback;

        if (!glfwInit())
            throw new RuntimeException("Could not initialize GLFW");

        final var monitor = glfwGetPrimaryMonitor();
        if (monitor == NULL)
            throw new RuntimeException("Could not get primary monitor");

        final var videoMode = glfwGetVideoMode(monitor);
        if (videoMode == null)
            throw new RuntimeException("Could not get video mode");

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        window = glfwCreateWindow(config.width, config.height, "gl-tetris-java", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Could not create window");

        glfwSetFramebufferSizeCallback(window, (w, width, height) -> {
            resizeSurface(width, height);
        });

        glfwSetWindowPos(window,
                (videoMode.width() - config.width) / 2,
                (videoMode.height() - config.height) / 2);
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        GL.createCapabilities();

        System.out.println("Surface created");
        System.out.println(glfwGetVersionString());
        System.out.println(glGetString(GL_VERSION));
        System.out.println(glGetString(GL_SHADING_LANGUAGE_VERSION));
        System.out.println(glGetString(GL_VENDOR));

        initializeCallback.initialize();

        glfwShowWindow(window);

        var lastUpdate = glfwGetTime();
        var delta = 0.0;
        while (!glfwWindowShouldClose(window)) {
            final var time = glfwGetTime();
            delta += time - lastUpdate;
            lastUpdate = time;
            while (delta >= .02) {
                delta -= .02;
                updateCallback.update(.02f);
            }
            renderCallback.render(time);
            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        glfwHideWindow(window);
        glfwDestroyWindow(window);
        glfwTerminate();
    }

    private static void resizeSurface(int width, int height) {
        surface.width = width;
        surface.height = height;
        surface.ratio = (float) width / height;
        System.out.println("Surface resized to " + surface.width + " x " + surface.height);

        var vw = surface.width;
        var vh = surface.height;
        if (surface.ratio > ratio)
            vw = (int) (vh * ratio);
        else
            vh = (int) (vw / ratio);
        var x = (surface.width - vw) / 2;
        var y = (surface.height - vh) / 2;
        setViewport(x, y, vw, vh);
    }

    private static void setViewport(int x, int y, int width, int height) {
        viewport.x = x;
        viewport.y = y;
        viewport.width = width;
        viewport.height = height;
        viewport.ratio = (float) width / height;
        glViewport(viewport.x, viewport.y, viewport.width, viewport.height);
        System.out.println("Viewport set to " + viewport.width + " x " + viewport.height);
    }

    @FunctionalInterface
    public interface InitializeCallback {
        void initialize();
    }

    @FunctionalInterface
    public interface UpdateCallback {
        void update(float delta);
    }

    @FunctionalInterface
    public interface RenderCallback {
        void render(double time);
    }

    public static class Config {
        public int width;
        public int height;
        public InitializeCallback initializeCallback;
        public UpdateCallback updateCallback;
        public RenderCallback renderCallback;
    }
}
