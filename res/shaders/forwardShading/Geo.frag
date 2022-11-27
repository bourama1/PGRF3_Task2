#version 330

in vec3 geoColor;

out vec4 outColor;

void main() {
    outColor = vec4(geoColor, 1.f);
}