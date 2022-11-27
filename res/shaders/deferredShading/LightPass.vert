#version 330

layout (location=0) in vec3 position;

//uniform mat4 u_View;
uniform mat4 u_Proj;
uniform mat4 u_Model;

void main()
{
    //gl_Position = u_Proj * u_View * u_Model * vec4(position, 1.0);
    gl_Position = u_Proj * u_Model * vec4(position, 1.0);
}