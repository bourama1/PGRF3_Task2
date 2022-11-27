#version 330
in vec2 inPosition;

uniform mat4 u_View;
uniform mat4 u_Proj;

out vec2 texCoords;
out vec3 toLightVector;

vec3 lightSoure = vec3(0.5, 0.5, 0.1);

vec3 getNormal() {
    // TODO: korektně implementovat
    return vec3(0., 0., 1.);
}

vec3 getTangent() {
    // TODO: implementovat
    return vec3(0);
}

void main() {
    texCoords = inPosition;


    vec4 objectPosition = u_View * vec4(inPosition, 0.f, 1.f);

    // Phong
    vec4 lightPosition = u_View * vec4(lightSoure, 1.);
    toLightVector = lightPosition.xyz - objectPosition.xyz;
    vec3 normalVector = transpose(inverse(mat3(u_View))) * getNormal();

    vec3 tangent = mat3(u_View) * getTangent();
    vec3 bitangent = cross(normalize(normalVector), normalize(tangent));

    // TODO: Vytvořit TBN matici
    mat3 tbn = mat3(1);

    // TODO: Aplikovat TBN na vektory, které se používají pro výpočet osvětlení


    gl_Position = u_Proj * objectPosition;
}

