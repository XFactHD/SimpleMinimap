#version 330
#extension GL_ARB_separate_shader_objects : require

#include <dynamictransforms.glsl>

uniform sampler2D Sampler0;

layout(location = 0) in vec2 texCoord0;
layout(location = 1) flat in ivec2 totalGrid;
#ifdef ROUND_MINIMAP
layout(location = 2) flat in ivec2 visibleGrid;
layout(location = 3) in vec2 relCoord;
#endif
layout(location = 4) flat in float gridLineWidth;

layout(location = 0) out vec4 fragColor;

void main() {
#ifdef ROUND_MINIMAP
    float visibleWidth = visibleGrid.x * 16.0;
    float totalWidth = totalGrid.x * 16.0;
    float radius = (visibleWidth / totalWidth) / 2.0;
    float dist = distance(relCoord, vec2(0.5));
    if (dist > radius) {
        discard;
    }
#endif

    vec4 color = texture(Sampler0, texCoord0);
    if (color.a == 0.0) {
        discard;
    }

    vec4 finalColor;
    if (gridLineWidth > 0.0) {
        vec2 texSize = vec2(totalGrid.x * 16.0, totalGrid.y * 16.0);
        vec2 chunkRel = mod((texCoord0 * texSize) + (gridLineWidth / 2.0), 16.0);
        if (chunkRel.x < gridLineWidth || chunkRel.y < gridLineWidth) {
            finalColor = mix(color, vec4(0.0, 0.0, 0.0, 1.0), 0.25);
        } else {
            finalColor = color;
        }
    } else {
        finalColor = color;
    }

    fragColor = finalColor * ColorModulator;
}
