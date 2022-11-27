import lwjglutils.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import transforms.*;

import java.io.IOException;
import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;

public class Renderer extends AbstractRenderer {
    private int shaderProgram;
    private Grid grid;
    private Camera camera;
    private boolean mouseButton1;
    private double ox, oy;
    private Mat4 projection;
    private OGLBuffers pointBuffer;

    @Override
    public void init() {
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glEnable(GL_DEPTH_TEST);
        glPointSize(10.f);

        camera = new Camera()
                .withPosition(new Vec3D(0.f, 0f, 0f))
                .withAzimuth(Math.PI * 1.25)
                .withZenith(Math.PI * -0.125)
                .withFirstPerson(false)
                .withRadius(3);
        projection = new Mat4PerspRH(Math.PI / 3, 600 / (float) 800, 0.1f, 1000.f);

        shaderProgram = ShaderUtils.loadProgram("/shaders/forwardShading/Geo");
        glUseProgram(shaderProgram);

        //Vertices
        float[] vertices = {
                0.f,0.f,0.f
        };

        //Indices
        int[] indices = {
                0
        };

        //OGLBuffers
        OGLBuffers.Attrib[] attributes = new OGLBuffers.Attrib[] {
                new OGLBuffers.Attrib("inPosition", 3),
        };
        pointBuffer = new OGLBuffers(vertices, attributes, indices);
    }

    @Override
    public void display() {
        renderMain();
    }

    public void renderMain(){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUseProgram(shaderProgram);

        // Proj
        int loc_uProj = glGetUniformLocation(shaderProgram, "u_Proj");
        glUniformMatrix4fv(loc_uProj, false, projection.floatArray());

        // View
        int loc_uView = glGetUniformLocation(shaderProgram, "u_View");
        glUniformMatrix4fv(loc_uView, false, camera.getViewMatrix().floatArray());

        pointBuffer.draw(GL_POINTS, shaderProgram);
    }

    private final GLFWCursorPosCallback cpCallbacknew = new GLFWCursorPosCallback() {
        @Override
        public void invoke(long window, double x, double y) {
            if (mouseButton1) {
                camera = camera.addAzimuth(Math.PI * (ox - x) / 800)
                        .addZenith(Math.PI * (oy - y) / 800);
                ox = x;
                oy = y;
            }
        }
    };

    private final GLFWMouseButtonCallback mbCallback = new GLFWMouseButtonCallback () {
        @Override
        public void invoke(long window, int button, int action, int mods) {
            mouseButton1 = glfwGetMouseButton(window, GLFW_MOUSE_BUTTON_1) == GLFW_PRESS;

            if (button==GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS){
                mouseButton1 = true;
                DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
                DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
                glfwGetCursorPos(window, xBuffer, yBuffer);
                ox = xBuffer.get(0);
                oy = yBuffer.get(0);
            }

            if (button==GLFW_MOUSE_BUTTON_1 && action == GLFW_RELEASE){
                mouseButton1 = false;
                DoubleBuffer xBuffer = BufferUtils.createDoubleBuffer(1);
                DoubleBuffer yBuffer = BufferUtils.createDoubleBuffer(1);
                glfwGetCursorPos(window, xBuffer, yBuffer);
                double x = xBuffer.get(0);
                double y = yBuffer.get(0);
                camera = camera.addAzimuth(Math.PI * (ox - x) / 800)
                        .addZenith(Math.PI * (oy - y) / 800);
                ox = x;
                oy = y;
            }
        }
    };

    private final GLFWScrollCallback scrollCallback = new GLFWScrollCallback() {
        @Override
        public void invoke(long window, double dx, double dy) {
            if (dy < 0)
                camera = camera.mulRadius(1.1f);
            else
                camera = camera.mulRadius(0.9f);

        }
    };

    @Override
    public GLFWScrollCallback getScrollCallback() {
        return scrollCallback;
    }

    @Override
    public GLFWMouseButtonCallback getMouseCallback() {
        return mbCallback;
    }

    @Override
    public GLFWCursorPosCallback getCursorCallback() {
        return cpCallbacknew;
    }
}
