#version 330

in vec2 inPosition;

out vec2 texCoords;
out vec3 viewVec;

uniform mat4 u_View;
uniform mat4 u_Proj;
uniform mat4 u_Model;

const float delta = 0.001f;

/**
*   @param inPosition vec2 vertex position in VB
*   Returns the position of the object as vec3
*/
vec3 posCalc(vec2 inPosition) {
    return vec3(inPosition, 0.f);
}

/**
*   @param inPosition vec2 vertex position in VB
*   Returns the matrix (tangent, bitangent, normal)
*/
mat3 getTBN(vec2 inPos) {
    vec3 tx = (posCalc(inPos + vec2(delta, 0)) - posCalc(inPos - vec2(delta, 0))) / vec3(1.f, 1.f, 2 * delta);
    vec3 ty = (posCalc(inPos + vec2(0, delta)) - posCalc(inPos - vec2(0, delta))) / vec3(1.f, 1.f, 2 * delta);
    vec3 normal = cross(tx, ty);
    normal = normalize(inverse(transpose(mat3(u_View * u_Model))) * normal);
    vec3 vTan = normalize(tx);
    vec3 vBi = cross(normal, vTan);
    vTan = cross(vBi, normal);
    return mat3(vTan, vBi, normal);
}

void main() {
    //Texture
    texCoords = inPosition.xy;

    //Object position
    vec4 worldPos = u_Model * vec4(posCalc(inPosition), 1.f);
    vec4 viewPos = u_View * worldPos;
    vec3 viewDirection = normalize(-viewPos.xyz);

    //TBN
    mat3 tbn = getTBN(inPosition);
    viewVec = transpose(tbn) * viewDirection;

    gl_Position = u_Proj * viewPos;
}