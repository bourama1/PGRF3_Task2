#version 330

layout (location = 0) out vec4 buffAlbedo;
layout (location = 1) out vec4 buffNormal;
layout (location = 2) out vec4 buffSpecular;

in vec2 texCoords;
in vec3 viewVec;

uniform sampler2D textureDiffuse;
uniform sampler2D textureSpecular;
uniform sampler2D textureNormal;
uniform sampler2D textureDisplacement;

void main()
{
    //Parallax mapping calculations
    float height = texture(textureDisplacement, texCoords).r;
    float scaleL = 0.04f;
    float scaleK = -0.02f;
    float v = height * scaleL + scaleK;
    vec3 eye = normalize(viewVec);
    vec2 offset = eye.xy * v;

    // Texture readings with parallax offset and store in gbuffer
    buffNormal = vec4(0.5f * (normalize(texture(textureNormal, texCoords + offset)).xyz) + 0.5f, 1.0f);
    // and the diffuse per-fragment color
    buffAlbedo = texture(textureDiffuse, texCoords);
    // store specular intensity in gAlbedoSpec's alpha component
    buffSpecular = texture(textureSpecular, texCoords);
}