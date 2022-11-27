import lwjglutils.ShaderUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import transforms.*;
import java.nio.DoubleBuffer;

import static utils.Const.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;

public class Renderer extends AbstractRenderer {
    private int geoShaderProgram;
    private Camera camera;
    private boolean mouseButton1;
    private double ox, oy;
    private Mat4 projection;
    private Mat4 model = new Mat4Identity();
    private GBuffer gBuffer;
    private Grid grid;
    private int loc_uLightSource;
    private float lightSourceX = 0.f, lightSourceY = 0.f, lightSourceZ = 1.f;

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
        projection = new Mat4PerspRH(Math.PI / 3, HEIGHT / (float) WIDTH, 0.1f, 1000.f);

        gBuffer = new GBuffer();
        grid = new Grid(10,10);

        //Shaders
        this.geoShaderProgram = ShaderUtils.loadProgram("/shaders/deferredShading/GeometryPass");
        glUseProgram(geoShaderProgram);
    }

    @Override
    public void display() {
        renderGeometry();
        renderLighting();
    }

    private void renderGeometry() {
        // Render G-Buffer for writing
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, gBuffer.getGBufferId());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, WIDTH, HEIGHT);
        glDisable(GL_BLEND);

        glUseProgram(geoShaderProgram);

        // Proj
        int loc_uProj = glGetUniformLocation(geoShaderProgram, "u_Proj");
        glUniformMatrix4fv(loc_uProj, false, projection.floatArray());

        // View
        int loc_uView = glGetUniformLocation(geoShaderProgram, "u_View");
        glUniformMatrix4fv(loc_uView, false, camera.getViewMatrix().floatArray());

        // View
        int loc_uModel = glGetUniformLocation(geoShaderProgram, "u_Model");
        glUniformMatrix4fv(loc_uModel, false, model.floatArray());

        // Light Source
        loc_uLightSource = glGetUniformLocation(geoShaderProgram, "u_LightSource");
        glUniform3f(loc_uLightSource, lightSourceX, lightSourceY, lightSourceZ);

        grid.getBuffers().draw(GL_TRIANGLE_STRIP, geoShaderProgram);

        glBindVertexArray(0);
        glEnable(GL_BLEND);
    }

    private void renderLighting() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, WIDTH, HEIGHT);

        glEnable(GL_BLEND);
        glBlendEquation(GL_FUNC_ADD);
        glBlendFunc(GL_ONE, GL_ONE);

        glBindFramebuffer(GL_READ_FRAMEBUFFER, gBuffer.getGBufferId());

        //TODO

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * Setting of LWJGL mouse position callback for camera movement
     */
    private final GLFWCursorPosCallback cpCallbacknew = new GLFWCursorPosCallback() {
        @Override
        public void invoke(long window, double x, double y) {
            if (mouseButton1) {
                camera = camera.addAzimuth(Math.PI * (ox - x) / WIDTH)
                        .addZenith(Math.PI * (oy - y) / WIDTH);
                ox = x;
                oy = y;
            }
        }
    };

    /**
     * Setting of LWJGL mouse button callback for camera movement
     */
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
                camera = camera.addAzimuth(Math.PI * (ox - x) / WIDTH)
                        .addZenith(Math.PI * (oy - y) / WIDTH);
                ox = x;
                oy = y;
            }
        }
    };

    /**
     * Setting of LWJGL scroll callback for camera zoom
     */
    private final GLFWScrollCallback scrollCallback = new GLFWScrollCallback() {
        @Override
        public void invoke(long window, double dx, double dy) {
            if (dy < 0)
                camera = camera.mulRadius(1 + CAM_SPEED);
            else
                camera = camera.mulRadius(1 - CAM_SPEED);

        }
    };

    /**
     * Setting of LWJGL key callbacks
     */
    private final GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods) {
            if(action != GLFW_RELEASE)
                return;
            switch (key) {
                case GLFW_KEY_ESCAPE -> glfwSetWindowShouldClose(window, true);
                // Rasterization mode
                case GLFW_KEY_G -> glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
                case GLFW_KEY_F -> glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
                case GLFW_KEY_H -> glPolygonMode(GL_FRONT_AND_BACK, GL_POINT);
                // Movement
                case GLFW_KEY_W -> camera = camera.forward(CAM_SPEED);
                case GLFW_KEY_S -> camera = camera.backward(CAM_SPEED);
                case GLFW_KEY_A -> camera = camera.left(CAM_SPEED);
                case GLFW_KEY_D -> camera = camera.right(CAM_SPEED);
            }
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

    @Override
    public GLFWKeyCallback getKeyCallback() {
        return keyCallback;
    }
}
