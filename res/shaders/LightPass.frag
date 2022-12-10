#version 330

const float SPECULAR_POWER = 10.f;
const int MAX_POINT_LIGHTS = 10;

const float constantAttenuation = 1.0f;
const float linearAttenuation = 0.1f;
const float quadraticAttenuation = 0.01f;

out vec4 fragColor;

in vec2 outTextCoord;

uniform mat4 u_Proj;
uniform sampler2D u_Albedo;
uniform sampler2D u_Specular;
uniform sampler2D u_Normal;
uniform sampler2D u_Depth;
uniform vec3 u_AmbientLightCol;
uniform float u_AmbientLightIntensity;
uniform vec3 u_PointLightsCol[MAX_POINT_LIGHTS];
uniform vec4 u_PointLightsPos[MAX_POINT_LIGHTS];
uniform float u_PointLightsIntensity[MAX_POINT_LIGHTS];

/**
*   @param ambientLight vec3 color of the ambient light
*   @param intensity float intensity of the ambient light
*   @param diffuse vec4 base color of the object
*   Returns the vec4 color which is mix of object color and ambient light
*/
vec4 calcAmbient(vec3 ambientLight, float intensity, vec4 diffuse) {
    return vec4(ambientLight, 1.0f) * diffuse * intensity;
}

/**
*   @param diffuse, specular vec4 diffuse/specular color of the object
*   @param reflectance float reflectance of the object
*   @param lightCol vec3 color of the current point light
*   @param position vec3 position of the object
*   @param intensity float intensity of the current point light
*   @param toLightDir/normal vec3 vectors to light and normal vector
*   Returns the vec4 color which is mix of diffuse and specular color of light
*/
vec4 calcLightColor(vec4 diffuse, vec4 specular, float reflectance, vec3 lightCol, vec3 position, float intensity, vec3 toLightDir, vec3 normal) {
    // Diffuse Light
    vec3 nd = normalize(normal);
    vec3 ld = normalize(toLightDir);
    float NDotL = max(dot(nd, ld), 0.f);
    vec4 diffuseColor = diffuse * vec4(lightCol, 1.0) * intensity * NDotL;

    // Specular Light
    vec3 viewVec = normalize(-position);
    vec3 fromLightDir = -toLightDir;
    vec3 reflected_light = normalize(reflect(fromLightDir, normal));
    float specularFactor = max(dot(viewVec, reflected_light), 0.0);
    specularFactor = pow(specularFactor, SPECULAR_POWER);
    vec4 specColor = specular * intensity * specularFactor * reflectance * vec4(lightCol, 1.0);

    return (diffuseColor + specColor);
}

/**
*   @param lightPos vec4 position of light for calculating vector to light
*   Returns the vec4 color which multiply the calculated light with attenuation
*/
vec4 calcPointLight(vec4 diffuse, vec4 specular, float reflectance, vec3 lightCol, vec4 lightPos, float intensity, vec3 position, vec3 normal) {
    vec3 lightVec = lightPos.xyz - position;
    vec3 toLightDir  = normalize(lightVec);
    vec4 light_color = calcLightColor(diffuse, specular, reflectance, lightCol, position, intensity, toLightDir, normal);

    //attenuation
    float dis = length(lightVec);
    float att = 1.0 / (constantAttenuation + linearAttenuation * dis + quadraticAttenuation * pow(dis, 2.0f));
    return light_color * att;
}

void main() {
    vec4 albedo = texture(u_Albedo, outTextCoord);
    vec4 diffuse = vec4(albedo.rgb, 1.0f);
    float reflectance = albedo.a;
    vec4 specular = texture(u_Specular, outTextCoord);

    vec3 normal  = normalize(texture(u_Normal, outTextCoord).xyz  * 2.0f - 1.0f);
    float depth = texture(u_Depth, outTextCoord).x * 2.0f - 1.0f;

    mat4 invProj = inverse(u_Proj);

    vec4 clip      = vec4(outTextCoord.x * 2.0 - 1.0, outTextCoord.y * 2.0 - 1.0, depth, 1.0);
    vec4 view_w    = invProj * clip;
    vec3 view_pos  = view_w.xyz / view_w.w;

    // combine all point lights together
    vec4 diffuseSpecularComp = vec4(0.0f);
    for (int i = 0; i < MAX_POINT_LIGHTS; i++) {
        if (u_PointLightsIntensity[i] > 0.0f)
                diffuseSpecularComp += calcPointLight(diffuse, specular, reflectance, u_PointLightsCol[i],
                                                u_PointLightsPos[i], u_PointLightsIntensity[i], view_pos, normal);
    }

    // combine ambient and object color together, then add them to point lights color
    vec4 ambient = calcAmbient(u_AmbientLightCol, u_AmbientLightIntensity, diffuse);
    fragColor = ambient + diffuseSpecularComp;
}