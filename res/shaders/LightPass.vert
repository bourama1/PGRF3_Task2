#version 330

layout (location=0) in vec2 inPosition;

out vec2 outTextCoord;

void main()
{
    outTextCoord = inPosition;
    vec2 inPos = inPosition * 2.0f - 1.0f;
    inPos.x = inPos.x + .5f;
    gl_Position = vec4(inPos, 0.f, 1.0f);
}