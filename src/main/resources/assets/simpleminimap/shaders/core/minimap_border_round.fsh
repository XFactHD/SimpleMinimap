#version 330
#extension GL_ARB_separate_shader_objects : require

#include <dynamictransforms.glsl>

uniform sampler2D Sampler0;

layout(location = 0) in vec2 texCoord0;
layout(location = 1) in vec4 vertexColor;

layout(location = 0) out vec4 fragColor;

void main() {
    // Extracted values don't need to be converted to their CPU-side range as they are only used relative to each other
    float mapRadius = vertexColor.a;
    float borderWidth = vertexColor.r;
    float texSize = vertexColor.g;
    float minInnerUV = borderWidth / texSize;
    float maxInnerUV = (texSize - borderWidth) / texSize;

    float outerRadius = 0.5;
    float innerRadius = mapRadius / ((mapRadius + borderWidth) * 2.0);
    float dist = distance(texCoord0, vec2(0.5));
    if (abs(dist) > outerRadius || abs(dist) < innerRadius) {
        discard;
    }

    float factor = 1.0 - ((abs(dist) - innerRadius) / (outerRadius - innerRadius));
    float u = mix(minInnerUV, maxInnerUV, texCoord0.x);
    float v;
    if (texCoord0.y < 0.5) {
        v = mix(0.0, minInnerUV, factor);
    } else {
        v = mix(maxInnerUV, 1.0, factor);
    }
    vec4 color = texture(Sampler0, vec2(u, v));
    if (color.a == 0.0) {
        discard;
    }
    fragColor = color * ColorModulator;
}
