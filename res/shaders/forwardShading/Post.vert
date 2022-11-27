#version 330
in vec2 inPosition;

out vec2 texCoords;

void main() {
    texCoords = inPosition;

    vec2 newPos = inPosition * 2 - 1;
    gl_Position = vec4(newPos, 0.5, 1.);
}