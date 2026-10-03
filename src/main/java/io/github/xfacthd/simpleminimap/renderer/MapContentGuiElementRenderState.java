package io.github.xfacthd.simpleminimap.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import io.github.xfacthd.simpleminimap.util.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.util.ARGB;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;

record MapContentGuiElementRenderState(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2fc pose,
        ScreenRectangle rect,
        float u0,
        float u1,
        float v0,
        float v1,
        int tilesWidth,
        int tilesHeight,
        int paddingTiles,
        float pixelsPerTexel,
        @Nullable ScreenRectangle scissorArea,
        @Nullable ScreenRectangle bounds
) implements GuiElementRenderState {
    MapContentGuiElementRenderState(
            GuiGraphicsExtractor graphics,
            RenderPipeline pipeline,
            MapAggregateTexture texture,
            ScreenRectangle rect,
            float u0,
            float u1,
            float v0,
            float v1,
            int visibleTilesWidth,
            int visibleTilesHeight,
            int paddingTiles
    ) {
        TextureSetup textureSetup = TextureSetup.singleTexture(texture.getTextureView(), texture.getSampler());
        Matrix3x2fc pose = new Matrix3x2f(graphics.pose());
        float gridLineWidth = computeLineWidth(texture, rect.width(), u0, u1);
        ScreenRectangle scissorArea = graphics.peekScissorStack();
        ScreenRectangle bounds = getBounds(rect, pose, scissorArea);
        this(pipeline, textureSetup, pose, rect, u0, u1, v0, v1, visibleTilesWidth, visibleTilesHeight, paddingTiles, gridLineWidth, scissorArea, bounds);
    }

    @Override
    public void buildVertices(VertexConsumer buffer) {
        vertex(buffer, rect.left(),  rect.top(),    u0, v0, 0x00, 0x00);
        vertex(buffer, rect.left(),  rect.bottom(), u0, v1, 0x00, 0xFF);
        vertex(buffer, rect.right(), rect.bottom(), u1, v1, 0xFF, 0xFF);
        vertex(buffer, rect.right(), rect.top(),    u1, v0, 0xFF, 0x00);
    }

    private void vertex(VertexConsumer buffer, float x, float y, float u, float v, int relX, int relY) {
        buffer.addVertexWith2DPose(pose, x, y);
        buffer.setUv(u, v);
        buffer.setUv1(tilesWidth, tilesHeight);
        if (pipeline == SimpleMinimapRenderPipelines.MAP_CONTENT_ROUND) {
            buffer.setUv2(tilesWidth - paddingTiles, tilesHeight - paddingTiles);
            buffer.setColor(ARGB.color(0, 0, relX, relY));
        }
        buffer.setLineWidth(pixelsPerTexel);
    }

    private static float computeLineWidth(MapAggregateTexture texture, int rectWidth, float u0, float u1) {
        int lineWidth = Config.GRID_LINE_WIDTH.getAsInt();
        if (lineWidth == 0) {
            return 0F;
        }

        int guiScale = Minecraft.getInstance().getWindow().getGuiScale();
        float sectionWidth = (u1 - u0) * texture.getWidth();
        float gridLineWidth = (lineWidth * 2) / ((rectWidth * guiScale) / sectionWidth);
        return gridLineWidth > lineWidth ? 0F : gridLineWidth;
    }

    private static @Nullable ScreenRectangle getBounds(ScreenRectangle rect, Matrix3x2fc pose, @Nullable ScreenRectangle scissorArea) {
        ScreenRectangle bounds = rect.transformMaxBounds(pose);
        return scissorArea != null ? scissorArea.intersection(bounds) : bounds;
    }
}
