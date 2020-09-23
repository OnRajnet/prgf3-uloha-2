#version 460
in vec2 inPosition;// input from the vertex buffer

out vec2 textureCoordinates;

void main() {
    textureCoordinates = inPosition;
    vec2 inPositionScaled = inPosition * 2 - 1;
    gl_Position = vec4(inPositionScaled.x, inPositionScaled.y, 0.0, 1.0);
}

//7. využijeme toho že je grid 0.0 do 1.1. Využijeme jako souřadnice do textury.
//8. grid -1.1, vynásobíš 2 a odečteš 1, aby jsi dostal do griru