package io.github.xfacthd.simpleminimap.renderer;

import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.RenderPipelines;

public interface MapBlitter {
    MapBlitter DEFAULT = (graphics, texture, sampler, rect, u0, u1, v0, v1) ->
            graphics.innerBlit(RenderPipelines.GUI_TEXTURED, texture, sampler, rect.left(), rect.top(), rect.right(), rect.bottom(), u0, u1, v0, v1, -1);

    void blit(GuiGraphicsExtractor graphics, GpuTextureView texture, GpuSampler sampler, ScreenRectangle rect, float u0, float u1, float v0, float v1);
}
