#version 460

in float z;
out vec4 outColor;// (vždy jediný) výstup z fragment shaderu

void main() {
    outColor = vec4(1.0, 1.0, 1.0, 1.0);
} 
