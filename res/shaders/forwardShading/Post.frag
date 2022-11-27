#version 330

in vec2 texCoords;

uniform sampler2D textureBase;

out vec4 outColor;

void main() {
    outColor = texture(textureBase, texCoords).rbga;
}