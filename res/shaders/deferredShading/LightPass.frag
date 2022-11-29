#version 330

out vec4 fragColor;

in vec2 outTextCoord;

struct Attenuation
{
    float constant;
    float linear;
    float exponent;
};

struct PointLight
{
    vec3 colour;
    vec3 position;
    float intensity;
    Attenuation att;
};

uniform mat4 u_InvView;
uniform mat4 u_InvProj;
uniform vec3 u_LightSource;
uniform sampler2D u_Albedo;
uniform sampler2D u_Normal;
uniform sampler2D u_Depth;

uniform vec2 screenSize;
uniform float specularPower;
uniform PointLight pointLight;

vec2 getTextCoord()
{
    return gl_FragCoord.xy / screenSize;
}

vec4 calcLightColour(vec4 baseColor, vec3 light_colour, float light_intensity, vec3 position, vec3 to_light_dir, vec3 normal)
{
    vec4 diffuseColour = vec4(0, 0, 0, 1);
    vec4 specColour = vec4(0, 0, 0, 1);

    // Diffuse Light
    float diffuseFactor = max(dot(normal, to_light_dir), 0.0);
    diffuseColour = baseColor * vec4(light_colour, 1.0) * light_intensity * diffuseFactor;

    // Specular Light
    vec3 camera_direction = normalize(-position);
    vec3 from_light_dir = -to_light_dir;
    vec3 reflected_light = normalize(reflect(from_light_dir , normal));
    float specularFactor = max( dot(camera_direction, reflected_light), 0.0);
    specularFactor = pow(specularFactor, specularPower);
    specColour = baseColor * light_intensity  * specularFactor * vec4(light_colour, 1.0);

    return (diffuseColour + specColour);
}

vec4 calcPointLight(vec4 baseColor, PointLight light, vec3 position, vec3 normal)
{
    vec3 light_direction = light.position - position;
    vec3 to_light_dir  = normalize(light_direction);
    vec4 light_colour = calcLightColour(baseColor, light.colour, light.intensity, position, to_light_dir, normal);

    // Apply Attenuation
    float distance = length(light_direction);
    float attenuationInv = light.att.constant + light.att.linear * distance +
    light.att.exponent * distance * distance;
    return light_colour / attenuationInv;
}

void main() {
    vec2 textCoord = getTextCoord();
    float depth = texture(u_Depth, textCoord).r;
    vec4 baseColor = texture(u_Albedo, textCoord);
    vec3 normal  = texture(u_Normal, textCoord).xyz;

    //fragColor = calcPointLight(baseColor, pointLight, worldPos.xyz, normal.xyz);
}