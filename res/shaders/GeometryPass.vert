#version 330

layout (location = 0) in vec4 inPosition;
layout (location = 1) in vec2 inTexCoord;
layout (location = 2) in vec3 inNormal;
in vec3 color;

out vec2 texCoords;
out vec3 viewVec;
out vec3 normal;
out vec3 lightCol;

uniform mat4 u_View;
uniform mat4 u_Proj;
uniform mat4 u_Model;
uniform int u_Obj;

const float delta = 0.001f;

/**
*   @param inPosition vec2 vertex position in VB
*   Returns the matrix (tangent, bitangent, normal)
*/
mat3 getTBN(vec3 inPos) {
    vec3 tx = ((inPos + vec3(delta, 0, 0)) - (inPos - vec3(delta, 0, 0))) / vec3(1.f, 1.f, 2 * delta);
    vec3 ty = ((inPos + vec3(0, delta, 0)) - (inPos - vec3(0, delta, 0))) / vec3(1.f, 1.f, 2 * delta);
    normal = cross(tx, ty);
    normal = normalize(inverse(transpose(mat3(u_View * u_Model))) * normal);
    vec3 vTan = normalize(tx);
    vec3 vBi = cross(normal, vTan);
    vTan = cross(vBi, normal);
    return mat3(vTan, vBi, normal);
}

void main() {
    if (u_Obj == 0) {
        // Lights are only rendered as points for info
        lightCol = color;
        normal = vec3(1.f);
        gl_Position = u_Proj * u_View * inPosition;
    } else if (u_Obj == 1) {
        // OBJ settings
        //Texture
        texCoords = inTexCoord;

        //Object position
        vec4 worldPos = u_Model * vec4(inPosition.xyz, 1.f);
        vec4 viewPos = u_View * worldPos;

        normal = normalize(inNormal);
        gl_Position = u_Proj * viewPos;
    } else if (u_Obj == 2) {
        // wall with textures settings
        //Texture
        texCoords = inPosition.xy;

        //Object position
        vec4 worldPos = u_Model * vec4(inPosition.xyz, 1.f);
        vec4 viewPos = u_View * worldPos;
        vec3 viewDirection = normalize(- viewPos.xyz);

        //TBN
        mat3 tbn = getTBN(inPosition.xyz);
        viewVec = transpose(tbn) * viewDirection;

        gl_Position = u_Proj * viewPos;
    }
}