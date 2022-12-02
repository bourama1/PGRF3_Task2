#version 330

layout (location = 0) in vec4 inPosition;
layout (location = 1) in vec2 inTexCoord;
layout (location = 2) in vec3 inNormal;

out vec2 texCoords;
out vec3 viewVec;
out vec3 normal;

uniform mat4 u_View;
uniform mat4 u_Proj;
uniform mat4 u_Model;
uniform int u_Obj;
uniform vec3 u_LightSourceGeometry;

const float delta = 0.001f;

/**
*   @param inPosition vec2 vertex position in VB
*   Returns the position of the object as vec3
*/
vec3 posCalc(vec3 inPosition) {
    switch(u_Obj) {
        case 2: return vec3(u_LightSourceGeometry.x + ((inPosition.x - 0.5f) / 4), u_LightSourceGeometry.y + ((inPosition.y - 0.5f) / 4), u_LightSourceGeometry.z);
    }
    return inPosition;
}

/**
*   @param inPosition vec2 vertex position in VB
*   Returns the matrix (tangent, bitangent, normal)
*/
mat3 getTBN(vec3 inPos) {
    vec3 tx = (posCalc(inPos + vec3(delta, 0, 0)) - posCalc(inPos - vec3(delta, 0, 0))) / vec3(1.f, 1.f, 2 * delta);
    vec3 ty = (posCalc(inPos + vec3(0, delta, 0)) - posCalc(inPos - vec3(0, delta, 0))) / vec3(1.f, 1.f, 2 * delta);
    normal = cross(tx, ty);
    normal = normalize(inverse(transpose(mat3(u_View * u_Model))) * normal);
    vec3 vTan = normalize(tx);
    vec3 vBi = cross(normal, vTan);
    vTan = cross(vBi, normal);
    return mat3(vTan, vBi, normal);
}

void main() {
    if(u_Obj == 1){
        //Texture
        texCoords = inTexCoord;

        //Object position
        vec4 worldPos = u_Model * vec4(posCalc(inPosition.xyz), 1.f);
        vec4 viewPos = u_View * worldPos;
        vec3 viewDirection = normalize(-viewPos.xyz);

        //TBN
        mat3 tbn = getTBN(inPosition.xyz);
        viewVec = transpose(tbn) * viewDirection;

        normal = normalize(inNormal);
        gl_Position = u_Proj * viewPos;
    } else {
        //Texture
        texCoords = inPosition.xy;

        //Object position
        vec4 worldPos = u_Model * vec4(posCalc(inPosition.xyz), 1.f);
        vec4 viewPos = u_View * worldPos;
        vec3 viewDirection = normalize(-viewPos.xyz);

        //TBN
        mat3 tbn = getTBN(inPosition.xyz);
        viewVec = transpose(tbn) * viewDirection;

        gl_Position = u_Proj * viewPos;
    }
}