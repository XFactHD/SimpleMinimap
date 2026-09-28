package io.github.xfacthd.simpleminimap.renderer;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import io.github.xfacthd.simpleminimap.util.Utils;
import net.minecraft.client.renderer.RenderPipelines;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

public final class SimpleMinimapRenderPipelines {
    public static final RenderPipeline MINIMAP_CONTENT_ROUND = RenderPipelines.GUI_TEXTURED.toBuilder()
            .withLocation(Utils.id("pipeline/minimap_content_round"))
            .withFragmentShader(Utils.id("core/minimap_content_round"))
            .build();
    public static final RenderPipeline MINIMAP_BORDER_ROUND = RenderPipelines.GUI_TEXTURED.toBuilder()
            .withLocation(Utils.id("pipeline/minimap_border_round"))
            .withFragmentShader(Utils.id("core/minimap_border_round"))
            .build();

    public static void onRegisterRenderPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(MINIMAP_CONTENT_ROUND);
        event.registerPipeline(MINIMAP_BORDER_ROUND);
    }

    private SimpleMinimapRenderPipelines() { }
}
