package lights;

import lwjglutils.OGLBuffers;

public class PointLight {
    private final float[] color;
    private final float[] position;
    private final float intensity;
    private final OGLBuffers buffers;

    public PointLight() {
        color = new float[]{
                (float) Math.random(), (float) Math.random(), (float) Math.random()
        };
        position = new float[]{
                (float) Math.random(), (float) Math.random(), (float) Math.random()*(1.f - 0.5f) + 0.5f, 1.0f
        };
        intensity = (float) Math.random() * 0.5f;

        float[] VB = new float[]{
                color[0], color[1], color[2],
                position[0], position[1], position[2], position[3]
        };

        OGLBuffers.Attrib[] attrs = new OGLBuffers.Attrib[]{
                new OGLBuffers.Attrib("color", 3),
                new OGLBuffers.Attrib("inPosition", 4),
        };

        this.buffers = new OGLBuffers(VB, attrs, null);
    }

    public float[] getColor() {
        return color;
    }

    public float[] getPosition() {
        return position;
    }

    public float getIntensity() {
        return intensity;
    }
    public OGLBuffers getBuffers() {
        return buffers;
    }
}
