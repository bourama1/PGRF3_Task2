#version 330

const float SPECULAR_POWER = 10.f;

const float constantAttenuation = 1.0f;
const float linearAttenuation = 0.1f;
const float quadraticAttenuation = 0.01f;

out vec4 fragColor;

in vec2 outTextCoord;

uniform mat4 u_View;
uniform mat4 u_Proj;
uniform vec3 u_LightSource;
uniform sampler2D u_Albedo;
uniform sampler2D u_Specular;
uniform sampler2D u_Normal;
uniform sampler2D u_Depth;
uniform int u_Obj;

vec4 calcLightColor(vec4 diffuse, vec4 specular, float reflectance, vec3 position, vec3 normal) {
    vec4 diffuseColor = vec4(0.f, 0.f, 0.f, 1.f);
    vec4 specColor = vec4(0.f, 0.f, 0.f, 1.f);
    vec4 ambientColor = vec4(0.1f, 0.1f, 0.1f, 1.f);

    // Diffuse Light
    vec3 toLightDir = normalize(u_LightSource.xyz - position.xyz);
    vec4 lightColor = vec4(0.7f, 0.7f, 0.7f, 1.0f);
    float diffuseFactor = max(dot(normal, toLightDir), 0.0f);
    diffuseColor = diffuse * lightColor * diffuseFactor;

    // Specular Light
    vec3 cameraDir = normalize(-position);
    vec3 fromLightDir = -toLightDir;
    vec3 reflectedLight = normalize(reflect(fromLightDir, normal));
    float specularFactor = max(dot(cameraDir, reflectedLight), 0.0f);
    specularFactor = pow(specularFactor, SPECULAR_POWER);
    specColor = specular * specularFactor * reflectance * lightColor;

    // Attenuation
    float dis = length(toLightDir);
    float att = 1.0 / (constantAttenuation + linearAttenuation * dis + quadraticAttenuation * pow(dis, 2.0f));

    return ambientColor + att * (diffuseColor + specColor);
}

void main() {
    vec4 albedo = texture(u_Albedo, outTextCoord);
    vec3 normal  = normalize(texture(u_Normal, outTextCoord).xyz  * 2.0f - 1.0f);
    float depth = texture(u_Depth, outTextCoord).r * 2.0f - 1.0f;

    mat4 invView = inverse(u_View);
    mat4 invProj = inverse(u_Proj);

    vec4 clip      = vec4(outTextCoord.x * 2.0 - 1.0, outTextCoord.y * 2.0 - 1.0, depth, 1.0);
    vec4 view_w    = invProj * clip;
    vec3 view_pos  = view_w.xyz / view_w.w;

    vec4 diffuse = vec4(albedo.rgb, 1.0f);
    float reflectance = albedo.a;
    vec4 specular = texture(u_Specular, outTextCoord);
    fragColor = calcLightColor(diffuse, specular, reflectance, view_pos, normal);

    // Light Object
    if (u_Obj == 2)
            fragColor = vec4(1.f);
}