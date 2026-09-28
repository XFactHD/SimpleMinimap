#version 330
#extension GL_ARB_separate_shader_objects : require

#include <dynamictransforms.glsl>

uniform sampler2D Sampler0;

layout(location = 0) in vec2 texCoord0;
layout(location = 1) in vec4 vertexColor;

layout(location = 0) out vec4 fragColor;

void main() {
    // Extracted values don't need to be converted to their CPU-side range as they are only used relative to each other
    float visibleTiles = vertexColor.a;
    float paddingTiles = vertexColor.r;
    float radius = (visibleTiles / (visibleTiles + paddingTiles)) / 2.0;
    vec2 contentCoord = vertexColor.gb;

    float dist = distance(contentCoord, vec2(0.5));
    if (dist > radius) {
        discard;
    }

    vec4 color = texture(Sampler0, texCoord0);
    if (color.a == 0.0) {
        discard;
    }
    fragColor = color * ColorModulator;
}
