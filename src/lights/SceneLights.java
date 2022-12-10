package lights;

import java.util.*;

public class SceneLights {
    private final AmbientLight ambientLight;
    private final List<PointLight> pointLights;

    public SceneLights() {
        ambientLight = new AmbientLight();
        pointLights = new ArrayList<>();
    }

    public AmbientLight getAmbientLight() {
        return ambientLight;
    }

    public List<PointLight> getPointLights() {
        return pointLights;
    }
}
