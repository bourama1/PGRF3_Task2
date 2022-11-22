#version 330

uniform mat4 u_Proj;
uniform mat4 u_View;

layout(points) in;
layout(triangle_strip, max_vertices = 3) out;

out vec3 geoColor;

void main() {
    geoColor = vec3(1.f,0.f,0.f);
    gl_Position = u_Proj * u_View *  gl_in[0].gl_Position;
    EmitVertex();

    geoColor = vec3(0.f,1.f,0.f);
    gl_Position = u_Proj * u_View *  vec4(gl_in[0].gl_Position.x, gl_in[0].gl_Position.y  - 0.5f, gl_in[0].gl_Position.zw);
    EmitVertex();

    geoColor = vec3(0.f,0.f,1.f);
    gl_Position = u_Proj * u_View *  vec4(gl_in[0].gl_Position.x + 0.5f, gl_in[0].gl_Position.yzw);
    EmitVertex();
    EndPrimitive();
}