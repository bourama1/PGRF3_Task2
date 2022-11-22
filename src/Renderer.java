import lwjglutils.OGLModelOBJ;
import lwjglutils.OGLRenderTarget;
import lwjglutils.OGLTexture2D;
import lwjglutils.ShaderUtils;
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
    private int shaderProgram, shaderProgramPost, shaderProgramEleph;
    private Grid grid, gridPost;

    private Camera camera;
    private OGLTexture2D textureBase;
    private OGLTexture2D textureNormal;
    private boolean mouseButton1;
    private double ox, oy;
    private Mat4 projection;

    // PostProcessing
    private OGLRenderTarget renderTarget;
    private OGLTexture2D.Viewer viewer;
    private OGLModelOBJ model;

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
        projection = new Mat4PerspRH(Math.PI / 3, 600 / (float) 800, 0.1f, 1000.f);

        shaderProgram = ShaderUtils.loadProgram("/shaders/Basic");
        shaderProgramPost = ShaderUtils.loadProgram("/shaders/Post");
        shaderProgramEleph = ShaderUtils.loadProgram("/shaders/Elephant");
        glUseProgram(shaderProgramEleph);

        // Color
        int loc_uColorR = glGetUniformLocation(shaderProgram, "u_ColorR");
        glUniform1f(loc_uColorR, 1.f);

        grid = new Grid(20, 20);
        gridPost = new Grid(2, 2);

        renderTarget = new OGLRenderTarget(800, 600);
        viewer = new OGLTexture2D.Viewer();
        model = new OGLModelOBJ("/obj/ElephantBody.obj");

        try {
            textureBase = new OGLTexture2D("./textures/mosaic.jpg");
            textureNormal = new OGLTexture2D("./textures/bricksn.png");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void display() {
        //renderMain();
        renderElephantBody();
        //renderPost();
        //renderTexturesView();
    }

    private void renderTexturesView() {
        viewer.view(textureBase, -1, -1, 0.5);
        viewer.view(textureNormal, -1, -0.5, 0.5);
        viewer.view(renderTarget.getColorTexture(), -1, 0, 0.5);
        viewer.view(renderTarget.getDepthTexture(), -1, 0.5, 0.5);
    }

    private void renderElephantBody() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        glUseProgram(shaderProgramEleph);

        // Model
        Mat4 transf = new Mat4Identity();
        transf = transf.mul(new Mat4RotY(Math.PI));
        transf = transf.mul(new Mat4Scale(0.02f));
        int loc_uModel = glGetUniformLocation(shaderProgramEleph, "u_Model");
        glUniformMatrix4fv(loc_uModel, false, transf.floatArray());

        // Proj
        int loc_uProj = glGetUniformLocation(shaderProgramEleph, "u_Proj");
        glUniformMatrix4fv(loc_uProj, false, projection.floatArray());

        // View
        int loc_uView = glGetUniformLocation(shaderProgramEleph, "u_View");
        glUniformMatrix4fv(loc_uView, false, camera.getViewMatrix().floatArray());

        model.getBuffers().draw(GL_TRIANGLES, shaderProgramEleph);
    }

    public void renderMain(){
        // Vykresluj do texture
        renderTarget.bind();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUseProgram(shaderProgram);

        // Proj
        int loc_uProj = glGetUniformLocation(shaderProgram, "u_Proj");
        glUniformMatrix4fv(loc_uProj, false, projection.floatArray());

        // View
        int loc_uView = glGetUniformLocation(shaderProgram, "u_View");
        glUniformMatrix4fv(loc_uView, false, camera.getViewMatrix().floatArray());

        textureBase.bind(shaderProgram, "textureBase", 0);
        textureNormal.bind(shaderProgram, "textureNormal", 1);
        grid.getBuffers().draw(GL_TRIANGLES, shaderProgram);
    }

    public void renderPost(){
        // Vykresluj na obrazovku
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        glUseProgram(shaderProgramPost);

        // Načíst texturu z render targetu
        renderTarget.getColorTexture().bind(shaderProgramPost, "textureBase", 0);

        // Render quad přes obrazovku
        gridPost.getBuffers().draw(GL_TRIANGLES, shaderProgramPost);
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
