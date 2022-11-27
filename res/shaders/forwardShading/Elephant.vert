#version 330

uniform mat4 u_Proj;
uniform mat4 u_View;
uniform mat4 u_Model;

in vec4 inPosition;
in vec2 inTexCoord;
in vec3 inNormal;

void main() {
    gl_Position = u_Proj * u_View * u_Model * inPosition;
}