import lights.PointLight;
import lights.SceneLights;
import lwjglutils.OGLModelOBJ;
import lwjglutils.OGLTexture2D;
import lwjglutils.ShaderUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;
import transforms.*;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

import static utils.Const.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*;

public class Renderer extends AbstractRenderer {
    private int geoShaderProgram, lightShaderProgram;
    private Camera camera;
    private boolean mouseButton1;
    private double ox, oy;
    private Mat4 projection;
    private Mat4 model = new Mat4Identity();
    private GBuffer gBuffer;
    private Grid grid, quadMesh;
    private OGLTexture2D.Viewer viewer;
    private OGLModelOBJ objModel;
    private SceneLights sceneLights;
    private int loc_uAmbientLight, loc_uAmbientLightIntensity;
    private int loc_uPointLightCol, loc_uPointLightIntensity, loc_uPointLightPos;
    private OGLTexture2D textureDiffuse, textureNormal, textureSpecular, textureHeight;


    @Override
    public void init() {
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glEnable(GL_DEPTH_TEST);
        glPointSize(10.f);

        //Shaders
        geoShaderProgram = ShaderUtils.loadProgram("/shaders/GeometryPass");
        lightShaderProgram = ShaderUtils.loadProgram("/shaders/LightPass");

        // Lights
        sceneLights = new SceneLights();
        for (int i = 0; i < POINT_LIGHTS; i++){
            sceneLights.getPointLights().add(new PointLight());
        }
        loc_uPointLightCol = glGetUniformLocation(lightShaderProgram, "u_PointLightsCol");
        loc_uPointLightPos = glGetUniformLocation(lightShaderProgram, "u_PointLightsPos");
        loc_uPointLightIntensity = glGetUniformLocation(lightShaderProgram, "u_PointLightsIntensity");
        loc_uAmbientLight = glGetUniformLocation(lightShaderProgram, "u_AmbientLightCol");
        loc_uAmbientLightIntensity = glGetUniformLocation(lightShaderProgram, "u_AmbientLightIntensity");

        camera = new Camera()
                .withPosition(new Vec3D(0.f, 0f, 0f))
                .withAzimuth(Math.PI * 1.25)
                .withZenith(Math.PI * -0.125)
                .withFirstPerson(false)
                .withRadius(3);
        projection = new Mat4PerspRH(Math.PI / 3, HEIGHT / (float) WIDTH, 0.1f, 1000.f);

        gBuffer = new GBuffer();
        grid = new Grid(10,10);
        quadMesh = new Grid(100,100);

        glUseProgram(geoShaderProgram);

        // Textures
        try {
            objModel = new OGLModelOBJ("/obj/ElephantBody.obj");
            textureDiffuse = new OGLTexture2D("./textures/bricks.png");
            textureSpecular = new OGLTexture2D("./textures/bricksSpec.png");
            textureNormal = new OGLTexture2D("./textures/bricksNormal.png");
            textureHeight = new OGLTexture2D("./textures/bricksHeight.png");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        viewer = new OGLTexture2D.Viewer();
    }

    @Override
    public void display() {
        renderGeometry();
        renderLighting();
        renderTextureView();
    }
    @Override
    public void cleanUp() {
        gBuffer.cleanUp();
    }

    private void renderGeometry() {
        // Render G-Buffer for writing
        glBindFramebuffer(GL_DRAW_FRAMEBUFFER, gBuffer.getGBufferId());
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, WIDTH, HEIGHT);
        glDisable(GL_BLEND);

        glUseProgram(geoShaderProgram);

        // Uniforms
        // Proj
        int loc_uProj = glGetUniformLocation(geoShaderProgram, "u_Proj");
        glUniformMatrix4fv(loc_uProj, false, projection.floatArray());

        // View
        int loc_uView = glGetUniformLocation(geoShaderProgram, "u_View");
        glUniformMatrix4fv(loc_uView, false, camera.getViewMatrix().floatArray());

        // Model
        int loc_uModel = glGetUniformLocation(geoShaderProgram, "u_Model");

        // Obj
        int loc_uObj = glGetUniformLocation(geoShaderProgram, "u_Obj");
        glUniform1i(loc_uObj, 1);
        model = model.mul(new Mat4Scale(0.01f));
        model = model.mul(new Mat4RotY(Math.PI));
        model = model.mul(new Mat4Transl(0.5f,0.5f,0.4f));
        glUniformMatrix4fv(loc_uModel, false, model.floatArray());

        objModel.getBuffers().draw(GL_TRIANGLES, geoShaderProgram);

        glUniform1i(loc_uObj, 0);
        for (PointLight light : sceneLights.getPointLights()){
            light.getBuffers().draw(GL_POINTS, geoShaderProgram);
        }

        // Wall
        glUniform1i(loc_uObj, 2);
        model = new Mat4Identity();
        glUniformMatrix4fv(loc_uModel, false, model.floatArray());
        textureDiffuse.bind(geoShaderProgram, "textureDiffuse", 0);
        textureSpecular.bind(geoShaderProgram, "textureSpecular", 1);
        textureNormal.bind(geoShaderProgram, "textureNormal",2);
        textureHeight.bind(geoShaderProgram, "textureHeight", 3);
        grid.getBuffers().draw(GL_TRIANGLE_STRIP, geoShaderProgram);

        glBindVertexArray(0);
        glEnable(GL_BLEND);
    }

    private void renderTextureView() {
        double pos = 0.5;
        for (int i = 0; i < gBuffer.getTextureIds().length; i++) {
            viewer.view(gBuffer.getTextureIds()[i], -1, pos, 0.5);
            pos -= 0.5;
        }
    }

    private void renderLighting() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, WIDTH, HEIGHT);

        glEnable(GL_BLEND);
        glBlendEquation(GL_FUNC_ADD);
        glBlendFunc(GL_ONE, GL_ONE);

        glBindFramebuffer(GL_READ_FRAMEBUFFER, gBuffer.getGBufferId());
        glUseProgram(lightShaderProgram);
        
        updateLights();

        // Uniforms
        // Proj
        int loc_uProj = glGetUniformLocation(lightShaderProgram, "u_Proj");
        glUniformMatrix4fv(loc_uProj, false, projection.floatArray());

        // View
        int loc_uView = glGetUniformLocation(lightShaderProgram, "u_View");
        glUniformMatrix4fv(loc_uView, false, camera.getViewMatrix().floatArray());

        // GBuffer
        int loc_uNormal = glGetUniformLocation(lightShaderProgram, "u_Normal");
        int loc_uAlbedo = glGetUniformLocation(lightShaderProgram, "u_Albedo");
        int loc_uSpecular = glGetUniformLocation(lightShaderProgram, "u_Specular");
        int loc_uDepth = glGetUniformLocation(lightShaderProgram, "u_Depth");

        // Bind the G-Buffer textures
        int[] textureIds = gBuffer.getTextureIds();
        int numTextures = textureIds != null ? textureIds.length : 0;
        for (int i = 0; i < numTextures; i++) {
            glActiveTexture(GL_TEXTURE0 + i);
            glBindTexture(GL_TEXTURE_2D, textureIds[i]);
        }

        glUniform1i(loc_uAlbedo,0);
        glUniform1i(loc_uNormal,1);
        glUniform1i(loc_uSpecular,2);
        glUniform1i(loc_uDepth,3);

        quadMesh.getBuffers().bind(lightShaderProgram);
        quadMesh.getBuffers().draw(GL_TRIANGLE_STRIP, lightShaderProgram);

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    private void updateLights() {
        glUseProgram(lightShaderProgram);
        float[] ambientLight = sceneLights.getAmbientLight().getColor();
        glUniform3fv(loc_uAmbientLight, ambientLight);
        float ambientLightIntensity = sceneLights.getAmbientLight().getIntensity();
        glUniform1f(loc_uAmbientLightIntensity, ambientLightIntensity);

        FloatBuffer pointLightColors = BufferUtils.createFloatBuffer(3 * POINT_LIGHTS);
        FloatBuffer pointLightPositions = BufferUtils.createFloatBuffer(4 * POINT_LIGHTS);
        FloatBuffer pointLightIntensity = BufferUtils.createFloatBuffer(POINT_LIGHTS);
        for (int i = 0; i < POINT_LIGHTS; i++) {
            float[] pointLightCol = sceneLights.getPointLights().get(i).getColor();
            float[] pointLightPos = sceneLights.getPointLights().get(i).getPosition();
            pointLightColors.put(pointLightCol);
            pointLightPositions.put(pointLightPos);
            pointLightIntensity.put(sceneLights.getPointLights().get(i).getIntensity());
        }
        pointLightColors.flip();
        pointLightPositions.flip();
        pointLightIntensity.flip();
        glUniform3fv(loc_uPointLightCol, pointLightColors);
        glUniform4fv(loc_uPointLightPos, pointLightPositions);
        glUniform1fv(loc_uPointLightIntensity, pointLightIntensity);
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
