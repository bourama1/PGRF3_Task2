#version 330

layout (location = 0) out vec3 buffPosition;
layout (location = 1) out vec3 buffNormal;
layout (location = 2) out vec4 buffAlbedo;

in vec2 TexCoords;
in vec4 FragPos;
in vec3 viewVec;

uniform sampler2D textureDiffuse;
uniform sampler2D textureSpecular;
uniform sampler2D textureNormal;
uniform sampler2D textureDisplacement;

void main()
{
    //Parallax mapping calculations
    float height = texture(textureDisplacement, TexCoords).r;
    float scaleL = 0.04f;
    float scaleK = -0.02f;
    float v = height * scaleL + scaleK;
    vec3 eye = normalize(viewVec);
    vec2 offset = eye.xy * v;

    // store the fragment position vector in the first gbuffer texture
    buffPosition = FragPos.xyz;
    //Texture readings with parallax offset and store in gbuffer
    buffNormal = normalize(texture(textureNormal, TexCoords + offset)).xyz;
    // and the diffuse per-fragment color
    buffAlbedo.rgb = texture(textureDiffuse, TexCoords).rgb;
    // store specular intensity in gAlbedoSpec's alpha component
    buffAlbedo.a = texture(textureSpecular, TexCoords).r;
}