#version 330
#extension GL_ARB_separate_shader_objects : require

#include <dynamictransforms.glsl>
#include <projection.glsl>

layout(location = 0) in vec3 Position;
layout(location = 1) in vec2 UV0;
layout(location = 2) in ivec2 UV1;
#ifdef ROUND_MINIMAP
layout(location = 3) in ivec2 UV2;
layout(location = 4) in vec4 Color;
#endif
layout(location = 5) in float LineWidth;

layout(location = 0) out vec2 texCoord0;
layout(location = 1) flat out ivec2 totalGrid;
#ifdef ROUND_MINIMAP
layout(location = 2) flat out ivec2 visibleGrid;
layout(location = 3) out vec2 relCoord;
#endif
layout(location = 4) flat out float gridLineWidth;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    texCoord0 = UV0;
    totalGrid = UV1;
#ifdef ROUND_MINIMAP
    visibleGrid = UV2;
    relCoord = Color.gb;
#endif
    gridLineWidth = LineWidth;
}
