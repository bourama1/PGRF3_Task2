import lwjglutils.OGLBuffers;
import lwjglutils.OGLRenderTarget;
import lwjglutils.OGLTexture2D;
import lwjglutils.ShaderUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import transforms.Camera;
import transforms.Mat4;
import transforms.Mat4PerspRH;
import transforms.Vec3D;

import java.io.IOException;
import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;

public class Renderer extends AbstractRenderer {
    private int shaderProgram;
    private int shaderProgramPost;
    private Grid grid, gridPost;

    private Camera camera;
    private Mat4 projection;
    private OGLTexture2D textureBase;
    private OGLTexture2D textureNormal;
    private boolean mouseButton1;
    private double ox, oy;

    // PostProcessing
    private OGLRenderTarget renderTarget;

    @Override
    public void init() {
        //glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        glEnable(GL_DEPTH_TEST);

        camera = new Camera()
                .withPosition(new Vec3D(0.f, 0f, 0f))
                .withAzimuth(Math.PI * 1.25)
                .withZenith(Math.PI * -0.125)
                .withFirstPerson(false)
                .withRadius(3);
        projection = new Mat4PerspRH(Math.PI / 3, 600 / (float) 800, 0.1f, 50.f);

        shaderProgram = ShaderUtils.loadProgram("/shaders/Basic");
        shaderProgramPost = ShaderUtils.loadProgram("/shaders/Post");
        glUseProgram(shaderProgram);

        // Color
        int loc_uColorR = glGetUniformLocation(shaderProgram, "u_ColorR");
        glUniform1f(loc_uColorR, 1.f);
        // Proj
        int loc_uProj = glGetUniformLocation(shaderProgram, "u_Proj");
        glUniformMatrix4fv(loc_uProj, false, projection.floatArray());

        grid = new Grid(20, 20);
        gridPost = new Grid(2, 2);

        renderTarget = new OGLRenderTarget(800, 600);

        try {
            textureBase = new OGLTexture2D("./textures/mosaic.jpg");
            textureNormal = new OGLTexture2D("./textures/bricksn.png");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void display() {
        renderMain();
        renderPost();
    }
    public void renderMain(){
        // Vykresluj do textury
        renderTarget.bind();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUseProgram(shaderProgram);

        // View
        int loc_uView = glGetUniformLocation(shaderProgram, "u_View");
        glUniformMatrix4fv(loc_uView, false, camera.getViewMatrix().floatArray());

        textureBase.bind(shaderProgram, "textureBase", 0);
        textureNormal.bind(shaderProgram, "textureNormal", 1);
        grid.getBuffers().draw(GL_TRIANGLES, shaderProgram);
    };
    public void renderPost(){
        // Zase vykresluj na obrazovku
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        glUseProgram(shaderProgramPost);

        // Načíst texturu z render targetu
        renderTarget.getColorTexture().bind(shaderProgramPost, "textureBase", 0);

        // Render quadu přes obrazovku
        gridPost.getBuffers().draw(GL_TRIANGLES, shaderProgramPost);
    };

    private GLFWCursorPosCallback cpCallbacknew = new GLFWCursorPosCallback() {
        @Override
        public void invoke(long window, double x, double y) {
            if (mouseButton1) {
                camera = camera.addAzimuth((double) Math.PI * (ox - x) / 800)
                        .addZenith((double) Math.PI * (oy - y) / 800);
                ox = x;
                oy = y;
            }
        }
    };

    private GLFWMouseButtonCallback mbCallback = new GLFWMouseButtonCallback () {
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
                camera = camera.addAzimuth((double) Math.PI * (ox - x) / 800)
                        .addZenith((double) Math.PI * (oy - y) / 800);
                ox = x;
                oy = y;
            }
        }
    };

    private GLFWScrollCallback scrollCallback = new GLFWScrollCallback() {
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
