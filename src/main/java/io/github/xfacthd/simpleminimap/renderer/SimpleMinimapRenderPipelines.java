package io.github.xfacthd.simpleminimap.renderer;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.vertex.VertexFormat;
import io.github.xfacthd.simpleminimap.util.Utils;
import net.minecraft.client.renderer.RenderPipelines;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

public final class SimpleMinimapRenderPipelines {
    private static final VertexFormat MAP_CONTENT_FORMAT = VertexFormat.builder(0)
            .addAttribute("Position", GpuFormat.RGB32_FLOAT) // Vertex position
            .addAttribute("UV0", GpuFormat.RG32_FLOAT)       // Texture coordinate
            .addAttribute("UV1", GpuFormat.RG16_SINT)        // Total grid size
            .addAttribute("LineWidth", GpuFormat.R32_FLOAT)  // Grid line width relative to one map texel
            .build();
    private static final VertexFormat MAP_CONTENT_FORMAT_ROUND = VertexFormat.builder(0)
            .addAttribute("Position", GpuFormat.RGB32_FLOAT) // Vertex position
            .addAttribute("UV0", GpuFormat.RG32_FLOAT)       // Texture coordinate
            .addAttribute("UV1", GpuFormat.RG16_SINT)        // Total grid size
            .addAttribute("UV2", GpuFormat.RG16_SINT)        // Visible grid size
            .addAttribute("Color", GpuFormat.RGBA8_UNORM)    // Relative content coordinate (G and B)
            .addAttribute("LineWidth", GpuFormat.R32_FLOAT)  // Grid line width relative to one map texel
            .build();
    private static final RenderPipeline.Snippet MAP_CONTENT = RenderPipelines.GUI_TEXTURED.toBuilder()
            .withLocation(Utils.id("pipeline/map_content"))
            .withVertexBinding(0, MAP_CONTENT_FORMAT)
            .withVertexShader(Utils.id("core/map_content"))
            .withFragmentShader(Utils.id("core/map_content"))
            .buildSnippet();
    public static final RenderPipeline MAP_CONTENT_SQUARE = RenderPipeline.builder(MAP_CONTENT)
            .withLocation(Utils.id("pipeline/minimap_content_square"))
            .build();
    public static final RenderPipeline MAP_CONTENT_ROUND = RenderPipeline.builder(MAP_CONTENT)
            .withLocation(Utils.id("pipeline/minimap_content_round"))
            .withShaderDefine("ROUND_MINIMAP")
            .withVertexBinding(0, MAP_CONTENT_FORMAT_ROUND)
            .build();
    public static final RenderPipeline MINIMAP_BORDER_ROUND = RenderPipelines.GUI_TEXTURED.toBuilder()
            .withLocation(Utils.id("pipeline/minimap_border_round"))
            .withFragmentShader(Utils.id("core/minimap_border_round"))
            .build();

    public static void onRegisterRenderPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(MAP_CONTENT_SQUARE);
        event.registerPipeline(MAP_CONTENT_ROUND);
        event.registerPipeline(MINIMAP_BORDER_ROUND);
    }

    private SimpleMinimapRenderPipelines() { }
}
