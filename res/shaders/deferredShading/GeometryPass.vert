#version 330

in vec2 inPosition;

out vec2 TexCoords;
out vec4 FragPos;
out vec3 Normal;
out vec3 viewVec;
out vec3 lightVec;

uniform mat4 u_View;
uniform mat4 u_Proj;
uniform mat4 u_Model;

uniform int u_Function;
uniform vec3 u_LightSource;

const float delta = 0.001f;

/**
*   @param inPosition vec2 vertex position in VB
*   Returns the position of the object as vec3
*/
vec3 posCalc(vec2 inPosition) {
    switch (u_Function) {
        default: return vec3(inPosition, 0.f);
    }
}

/**
*   @param inPosition vec2 vertex position in VB
*   Returns the matrix (tangent, bitangent, normal)
*/
mat3 getTBN(vec2 inPos) {
    vec3 tx = (posCalc(inPos + vec2(delta, 0)) - posCalc(inPos - vec2(delta, 0))) / vec3(1.f, 1.f, 2 * delta);
    vec3 ty = (posCalc(inPos + vec2(0, delta)) - posCalc(inPos - vec2(0, delta))) / vec3(1.f, 1.f, 2 * delta);
    Normal = cross(tx, ty);
    Normal = normalize(inverse(transpose(mat3(u_View * u_Model))) * Normal);
    vec3 vTan = normalize(tx);
    vec3 vBi = cross(Normal, vTan);
    vTan = cross(vBi, Normal);
    return mat3(vTan, vBi, Normal);
}

void main() {
    //Texture
    TexCoords = inPosition.xy;

    //Object position
    FragPos = u_View * u_Model * vec4(posCalc(inPosition), 1.f);

    //Light
    vec3 viewDirection = normalize(-FragPos.xyz);
    vec4 lightPosition = u_View * u_Model * vec4(u_LightSource, 1.);
    vec3 toLightVector = normalize(lightPosition.xyz - FragPos.xyz);

    //TBN
    mat3 tbn = getTBN(inPosition);
    viewVec = transpose(tbn) * viewDirection;
    lightVec = transpose(tbn) * toLightVector;

    gl_Position = u_Proj * FragPos;
}