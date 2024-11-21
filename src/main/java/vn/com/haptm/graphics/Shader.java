package vn.com.haptm.graphics;

import org.lwjgl.opengl.GL20;
import vn.com.haptm.mat4f.Mat4f;
import vn.com.haptm.util.ResourceManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_TRUE;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;

public class Shader {
    private static int binded;
    private final int program;
    private final Map<String, Integer> uniformsLocations = new HashMap<>();

//    public Shader(String... resources) {
//        final List<Integer> shaders = Stream.of(resources).map(this::createShader).toList();
//        program = glCreateProgram();
//        shaders.forEach(shader -> glAttachShader(program, shader));
//        glLinkProgram(program);
//        final int status = glGetProgrami(program, GL_LINK_STATUS);
//        shaders.forEach(GL20::glDeleteShader);
//        if (status != GL_TRUE) {
//            final String infoLog = glGetProgramInfoLog(program);
//            System.err.println("Error linking program: " + infoLog);
//            glDeleteProgram(program);
//        }
//    }

    private static final String vertexShaderSource = """
    #version 330
            
                layout(location = 0) in vec4 position_texcoords;
                layout(location = 1) in vec4 color;
            
                uniform mat4 u_projection;
            
                out DATA {
                    vec2 texcoords;
                    vec4 color;
                } vo;
            
                void main() {
                    gl_Position = vec4(position_texcoords.xy, 0, 1) * u_projection;
                    vo.texcoords = position_texcoords.zw;
                    vo.color = color;
                }
""";

    private static final String fragmentShaderSource = """
            #version 330
            
            out vec4 color;
            
            uniform sampler2D u_material;
            
            in DATA {
                vec2 texcoords;
                vec4 color;
            } fi;
            
            void main() {
                color = texture2D(u_material, fi.texcoords) * fi.color;
            }
            """;

    public Shader() {
        // Tạo shader từ các chuỗi nguồn mã shader
        int vertexShader = createShader(GL_VERTEX_SHADER, vertexShaderSource);
        int fragmentShader = createShader(GL_FRAGMENT_SHADER, fragmentShaderSource);

        program = glCreateProgram();

        // Gắn các shader vào chương trình
        glAttachShader(program, vertexShader);
        glAttachShader(program, fragmentShader);

        // Liên kết chương trình
        glLinkProgram(program);

        // Kiểm tra trạng thái liên kết
        int status = glGetProgrami(program, GL_LINK_STATUS);

        // Xóa các shader sau khi đã liên kết
        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);

        // Kiểm tra lỗi nếu có
        if (status != GL_TRUE) {
            String infoLog = glGetProgramInfoLog(program);
            System.err.println("Error linking program: " + infoLog);
            glDeleteProgram(program);
        }
    }

    // Phương thức tạo shader từ chuỗi mã nguồn
    private int createShader(int type, String source) {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);

        int compileStatus = glGetShaderi(shader, GL_COMPILE_STATUS);
        if (compileStatus != GL_TRUE) {
            String infoLog = glGetShaderInfoLog(shader);
            System.err.println("Error compiling shader: " + infoLog);
            glDeleteShader(shader);
            throw new RuntimeException("Shader compilation failed");
        }
        return shader;
    }

    public static void unbind() {
        if (binded == 0)
            return;
        binded = 0;
        glUseProgram(0);
    }

    public void bind() {
        if (binded == program)
            return;
        binded = program;
        glUseProgram(program);
    }

    private int getUniformLocation(String name) {
        if (uniformsLocations.containsKey(name))
            return uniformsLocations.get(name);
        final int location = glGetUniformLocation(program, name);
        uniformsLocations.put(name, location);
        return location;
    }

    public void setUniform1i(String name, int x) {
        glUniform1i(getUniformLocation(name), x);
    }

    public void setUniformMat4f(String name, Mat4f value) {
        glUniformMatrix4fv(getUniformLocation(name), false, value.array);
    }
}
