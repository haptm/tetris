package vn.com.haptm.inputs;

import org.lwjgl.glfw.GLFW;
import vn.com.haptm.graphics.Graphics;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwSetInputMode;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;

public final class Inputs {
    private static float time;
    private static Map<Integer, KeyState> keyState = new HashMap<>();
    private static Queue<Event> pool = new ConcurrentLinkedQueue<>();

    private Inputs() {
    }

    public static void initialize() {
        glfwSetInputMode(Graphics.window, GLFW.GLFW_STICKY_KEYS, GLFW.GLFW_TRUE);
        glfwSetKeyCallback(Graphics.window, (window, key, scancode, action, mods) -> {
            switch (action) {
                case GLFW_PRESS:
                case GLFW_RELEASE:
                    pool.offer(new Event(action, key));
                    break;
            }
        });
    }

    public static void poolEvents(float delta) {
        time += delta;
        var size = pool.size();
        while (size-- > 0) {
            final var event = pool.poll();
            if (event == null)
                break;
            var state = keyState.get(event.key);
            if (state == null)
                keyState.put(event.key, state = new KeyState());
            switch (event.action) {
                case GLFW_PRESS:
                    if (state.state != State.Released)
                        break;
                    state.state = State.JustPressed;
                    state.time = time;
                    break;
                case GLFW_RELEASE:
                    state.state = State.Released;
                    state.time = time;
                    break;
            }
        }
    }

    public static boolean isDown(int code, float repeat1, float repeatN) {
        final KeyState state = keyState.get(code);
        if (state == null)
            return false;
        if (isJustPressed(code)) {
            state.repeat = time + repeat1;
            return true;
        }
        if (state.state == State.Down) {
            if (time >= state.repeat) {
                state.repeat = time + repeatN;
                return true;
            }
        }
        return false;
    }

    public static boolean isJustPressed(int code) {
        final KeyState state = keyState.get(code);
        if (state == null)
            return false;
        if (state.state == State.JustPressed) {
            state.state = State.Down;
            return true;
        }
        return false;
    }

    private enum State {
        JustPressed, Down, Released
    }

    private static class KeyState {
        private State state = State.Released;
        private float time = 0;
        private float repeat = 0;
    }

    private static class Event {
        private final int action;
        private final int key;

        public Event(int action, int key) {
            this.action = action;
            this.key = key;
        }
    }
}
