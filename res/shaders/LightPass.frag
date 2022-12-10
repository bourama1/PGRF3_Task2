#version 330

const float SPECULAR_POWER = 10.f;
const int MAX_POINT_LIGHTS = 10;

const float constantAttenuation = 1.0f;
const float linearAttenuation = 0.1f;
const float quadraticAttenuation = 0.01f;

out vec4 fragColor;

in vec2 outTextCoord;

uniform mat4 u_View;
uniform mat4 u_Proj;
uniform sampler2D u_Albedo;
uniform sampler2D u_Specular;
uniform sampler2D u_Normal;
uniform sampler2D u_Depth;
uniform int u_Obj;
uniform vec3 u_AmbientLightCol;
uniform float u_AmbientLightIntensity;
uniform vec3 u_PointLightsCol[MAX_POINT_LIGHTS];
uniform vec4 u_PointLightsPos[MAX_POINT_LIGHTS];
uniform float u_PointLightsIntensity[MAX_POINT_LIGHTS];

vec4 calcAmbient(vec3 ambientLight, float intensity, vec4 diffuse) {
    return vec4(ambientLight, 1.0f) * diffuse * intensity;
}

vec4 calcLightColor(vec4 diffuse, vec4 specular, float reflectance, vec3 lightCol, vec3 position, float intensity, vec3 toLightDir, vec3 normal) {
    vec4 diffuseColor = vec4(0, 0, 0, 1);
    vec4 specColor = vec4(0, 0, 0, 1);

    // Diffuse Light
    float diffuseFactor = max(dot(normal, toLightDir), 0.0);
    diffuseColor = diffuse * vec4(lightCol, 1.0) * intensity * diffuseFactor;

    // Specular Light
    vec3 camera_direction = normalize(-position);
    vec3 from_light_dir = -toLightDir;
    vec3 reflected_light = normalize(reflect(from_light_dir, normal));
    float specularFactor = max(dot(camera_direction, reflected_light), 0.0);
    specularFactor = pow(specularFactor, SPECULAR_POWER);
    specColor = specular * intensity * specularFactor * reflectance * vec4(lightCol, 1.0);

    return (diffuseColor + specColor);
}

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

    mat4 invView = inverse(u_View);
    mat4 invProj = inverse(u_Proj);

    vec4 clip      = vec4(outTextCoord.x * 2.0 - 1.0, outTextCoord.y * 2.0 - 1.0, depth, 1.0);
    vec4 view_w    = invProj * clip;
    vec3 view_pos  = view_w.xyz / view_w.w;

    vec4 diffuseSpecularComp = vec4(0.0f);
    for (int i = 0; i < MAX_POINT_LIGHTS; i++) {
        if (u_PointLightsIntensity[i] > 0.0f)
                diffuseSpecularComp += calcPointLight(diffuse, specular, reflectance, u_PointLightsCol[i],
                                                u_PointLightsPos[i], u_PointLightsIntensity[i], view_pos, normal);
    }

    vec4 ambient = calcAmbient(u_AmbientLightCol, u_AmbientLightIntensity, diffuse);
    fragColor = ambient + diffuseSpecularComp;

    // Light Object
    if (u_Obj == 2)
            fragColor = vec4(1.f);
}