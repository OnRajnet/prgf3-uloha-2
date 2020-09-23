#version 460

in vec2 textureCoordinates;

out vec2 uv;

uniform float unit;
uniform float damping;
uniform sampler2D waveHeightTexture;


float leftHeight() {
    return texture(waveHeightTexture, vec2(textureCoordinates.x - unit, textureCoordinates.y)).x;
}

float rightHeight() {
    return texture(waveHeightTexture, vec2(textureCoordinates.x + unit, textureCoordinates.y)).x;
}

float downHeight() {
    return texture(waveHeightTexture, vec2(textureCoordinates.x, textureCoordinates.y - unit)).x;
}

float upHeight() {
    return texture(waveHeightTexture, vec2(textureCoordinates.x, textureCoordinates.y + unit)).x;
}

float currentHeight() {
    return texture(waveHeightTexture, textureCoordinates).x;
}

float currentWave() {
    return texture(waveHeightTexture, textureCoordinates).y;
}

void main() {
    float height = currentHeight();
    float wave = currentWave();
    wave += ((leftHeight() + rightHeight() + downHeight() + upHeight()) / 4.0) - height;
    wave *= damping;
    height += wave;

    uv = vec2(height, wave);
}