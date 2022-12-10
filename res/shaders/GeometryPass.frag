#version 330

layout (location = 0) out vec4 buffAlbedo;
layout (location = 1) out vec4 buffNormal;
layout (location = 2) out vec4 buffSpecular;

in vec2 texCoords;
in vec3 viewVec;
in vec3 normal;
in vec3 lightCol;

uniform sampler2D textureDiffuse;
uniform sampler2D textureSpecular;
uniform sampler2D textureNormal;
uniform sampler2D textureHeight;
uniform int u_Obj;

void main()
{
    if (u_Obj == 0) {
        // Texture readings and store in gbuffer
        buffNormal = vec4(normal, 1.0f) * 0.5f + 0.5f;
        // and the diffuse per-fragment color
        buffAlbedo = vec4(lightCol, 1.0f);
        // store specular per-fragment color
        buffSpecular = vec4(1.0f);
    } else if (u_Obj == 1) {
        // Texture readings and store in gbuffer
        buffNormal = vec4(normal, 1.0f) * 0.5f + 0.5f;
        // and the diffuse per-fragment color
        buffAlbedo = vec4(0.5f, 0.1f, 0.1f, 1.0f);
        // store specular per-fragment color
        buffSpecular = vec4(0.7f, 0.7f, 0.7f, 1.0f);
    } else if (u_Obj == 2) {
        // Calculations for wall with textures
        // Parallax mapping calculations
        float height = texture(textureHeight, texCoords).r;
        float scaleL = 0.04f;
        float scaleK = -0.02f;
        float v = height * scaleL + scaleK;
        vec3 eye = normalize(viewVec);
        vec2 offset = eye.xy * v;

        // Texture readings with parallax offset and store in gbuffer
        buffNormal = texture(textureNormal, texCoords + offset) * 0.5f + 0.5f;
        // and the diffuse per-fragment color
        buffAlbedo = texture(textureDiffuse, texCoords);
        // store specular per-fragment color
        buffSpecular = texture(textureSpecular, texCoords);
    }
}