#version 460
in vec2 inPosition; // input from the vertex buffer

uniform mat4 viewProj;
uniform sampler2D waveHeightTexture;

out float z;

void main() {
	z = texture(waveHeightTexture, inPosition).x;
	vec4 pos4 = vec4(inPosition.x, inPosition.y, z, 1.0);
	gl_Position = viewProj * pos4;
}

// kreslíme na obrazovku
// v x máme uložené Z