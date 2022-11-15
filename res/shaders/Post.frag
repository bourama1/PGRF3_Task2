#version 330

in vec2 texCoords;

uniform sampler2D textureBase;

out vec4 outColor;

void main() {

    if(gl_FragCoord.x > 400)
        discard;


    if(gl_FragCoord.y < 100 || gl_FragCoord.y > 250)
        outColor = texture(textureBase, texCoords).rbga;
    else
        outColor = texture(textureBase, texCoords).rgba;
}