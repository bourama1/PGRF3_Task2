#version 330

layout (location = 0) out vec4 buffAlbedo;
layout (location = 1) out vec4 buffNormal;
layout (location = 2) out vec4 buffSpecular;

in vec2 texCoords;
in vec3 viewVec;
in vec3 normal;

uniform sampler2D textureDiffuse;
uniform sampler2D textureSpecular;
uniform sampler2D textureNormal;
uniform sampler2D textureDisplacement;
uniform int u_Obj;

void main()
{
    if (u_Obj == 1) {
        // Texture readings with parallax offset and store in gbuffer
        buffNormal = vec4(0.5f * (normalize(normal)) + 0.5f, 1.0f);
        // and the diffuse per-fragment color
        buffAlbedo = vec4(0.8f, 0.4f, 0.4f, 1.0f);
        // store specular per-fragment color
        buffSpecular = vec4(0.5f, 0.5f, 0.5f, 1.0f);
    } else if (u_Obj == 2) {
        // Texture readings with parallax offset and store in gbuffer
        buffNormal = vec4(0.5f * (normalize(normal)) + 0.5f, 1.0f);
        // and the diffuse per-fragment color
        buffAlbedo = vec4(1.0f);
        // store specular per-fragment color
        buffSpecular = vec4(1.0f);
    } else {
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
        // store specular per-fragment color
        buffSpecular = texture(textureSpecular, texCoords);
    }
}