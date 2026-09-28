package io.github.xfacthd.simpleminimap.gui.hud;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import io.github.xfacthd.simpleminimap.renderer.SimpleMinimapRenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.util.ARGB;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;

record RoundMinimapContentGuiElementRenderState(
        TextureSetup textureSetup,
        Matrix3x2fc pose,
        int x0,
        int y0,
        int x1,
        int y1,
        float u0,
        float u1,
        float v0,
        float v1,
        int visibleTiles,
        int paddingTiles,
        @Nullable ScreenRectangle scissorArea,
        @Nullable ScreenRectangle bounds
) implements GuiElementRenderState {
    RoundMinimapContentGuiElementRenderState(
            GuiGraphicsExtractor graphics,
            GpuTextureView texture,
            GpuSampler sampler,
            ScreenRectangle rect,
            float u0,
            float u1,
            float v0,
            float v1,
            int visibleTiles,
            int paddingTiles
    ) {
        TextureSetup textureSetup = TextureSetup.singleTexture(texture, sampler);
        Matrix3x2fc pose = new Matrix3x2f(graphics.pose());
        ScreenRectangle scissorArea = graphics.peekScissorStack();
        ScreenRectangle bounds = getBounds(rect, pose, scissorArea);
        this(textureSetup, pose, rect.left(), rect.top(), rect.right(), rect.bottom(), u0, u1, v0, v1, visibleTiles, paddingTiles, scissorArea, bounds);
    }

    @Override
    public RenderPipeline pipeline() {
        return SimpleMinimapRenderPipelines.MINIMAP_CONTENT_ROUND;
    }

    @Override
    public void buildVertices(VertexConsumer buffer) {
        buffer.addVertexWith2DPose(pose, x0, y0).setUv(u0, v0).setColor(ARGB.color(visibleTiles, paddingTiles, 0x00, 0x00));
        buffer.addVertexWith2DPose(pose, x0, y1).setUv(u0, v1).setColor(ARGB.color(visibleTiles, paddingTiles, 0x00, 0xFF));
        buffer.addVertexWith2DPose(pose, x1, y1).setUv(u1, v1).setColor(ARGB.color(visibleTiles, paddingTiles, 0xFF, 0xFF));
        buffer.addVertexWith2DPose(pose, x1, y0).setUv(u1, v0).setColor(ARGB.color(visibleTiles, paddingTiles, 0xFF, 0x00));
    }

    private static @Nullable ScreenRectangle getBounds(ScreenRectangle rect, Matrix3x2fc pose, @Nullable ScreenRectangle scissorArea) {
        ScreenRectangle bounds = rect.transformMaxBounds(pose);
        return scissorArea != null ? scissorArea.intersection(bounds) : bounds;
    }
}
