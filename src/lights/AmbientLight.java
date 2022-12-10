package lights;

public class AmbientLight {
    private final float[] color;
    private final float intensity;

    public AmbientLight(float[] color, float intensity) {
        this.color = color;
        this.intensity = intensity;
    }

    public AmbientLight() {
        this(new float[]{0.2f, 0.2f, 0.2f}, 1.0f);
    }

    public float[] getColor() {
        return color;
    }

    public float getIntensity() {
        return intensity;
    }
}
