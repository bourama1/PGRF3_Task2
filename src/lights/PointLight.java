package lights;

public class PointLight {
    private final float[] color;
    private final float[] position;
    private final float intensity;

    public PointLight() {
        color = new float[]{
                (float) Math.random(), (float) Math.random(), (float) Math.random()
        };
        position = new float[]{
                (float) Math.random(), (float) Math.random(), (float) Math.random(), 1.0f
        };
        intensity = (float) Math.random();
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
}
