package io.github.xfacthd.simpleminimap.gui.hud;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import io.github.xfacthd.simpleminimap.data.MapChunk;
import io.github.xfacthd.simpleminimap.renderer.MapChunkProvider;
import io.github.xfacthd.simpleminimap.renderer.MapRenderer;
import io.github.xfacthd.simpleminimap.renderer.SimpleMinimapRenderPipelines;
import io.github.xfacthd.simpleminimap.util.Config;
import io.github.xfacthd.simpleminimap.util.Utils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.client.gui.GuiLayer;
import org.jspecify.annotations.Nullable;

public final class MinimapLayer implements GuiLayer {
    public static final int MIN_MAP_SIZE = 64;
    public static final int MAX_MAP_SIZE = 400;
    public static final int DEFAULT_MAP_SIZE = 96;
    public static final int MIN_MAP_OFFSET = 0;
    public static final int MAX_MAP_OFFSET = 100;
    public static final int DEFAULT_MAP_OFFSET = 7;
    static final int MINIMAP_BORDER_WIDTH = 3;
    /// Map edge length in chunks at the default zoom level
    private static final int DEFAULT_TILE_COUNT = 8;
    /// Padding around the primary visible tiles
    private static final int BASE_PADDING = 2;
    private static final int FRAME_TEX_SIZE = 40;
    private static final Identifier SQUARE_FRAME = Utils.id("square_frame");
    private static final Identifier PLAYER_MARKER = Utils.id("player_marker");
    @Nullable
    private static MapRenderer mapRenderer;
    private static int lastVisibleTileCount = -1;
    private static int lastPaddingTileCount = -1;

    @Override
    public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || Minecraft.getInstance().gui.hud.isHidden()) {
            return;
        }

        MapConfig config = computeMapConfig(graphics);
        float mapRot = 180F - player.getViewYRot(deltaTracker.getGameTimeDeltaPartialTick(false));
        int halfTileCount = config.getTotalTiles() / 2;
        int centerX = SectionPos.blockToSectionCoord(player.getBlockX());
        int centerZ = SectionPos.blockToSectionCoord(player.getBlockZ());
        long minChunk = ChunkPos.pack(centerX - halfTileCount, centerZ - halfTileCount);
        long maxChunk = ChunkPos.pack(centerX + halfTileCount, centerZ + halfTileCount);
        int blockOffX = Mth.positiveModulo(player.getBlockX(), MapChunk.CHUNK_SIZE);
        int blockOffZ = Mth.positiveModulo(player.getBlockZ(), MapChunk.CHUNK_SIZE);

        ScreenRectangle rect = prepareRenderArea(graphics, config, mapRot);
        RenderPipeline pipeline = config.round ? SimpleMinimapRenderPipelines.MAP_CONTENT_ROUND : SimpleMinimapRenderPipelines.MAP_CONTENT_SQUARE;
        getMapRenderer(config).extract(graphics, MapChunkProvider.DEFAULT, pipeline, minChunk, maxChunk, blockOffX, blockOffZ, rect);
        teardownRenderArea(graphics, config);

        extractMapBorder(graphics, config);
        extractMapDecorations(graphics, config, mapRot);
    }

    private static MapConfig computeMapConfig(GuiGraphicsExtractor graphics) {
        MinimapPosition position = Config.getMinimapPosition();
        int mapSize = Config.MINIMAP_SIZE.getAsInt();
        float scale = Config.MINIMAP_SCALE.get().value;
        boolean round = Config.MINIMAP_ROUND.getAsBoolean();
        boolean rotate = Config.MINIMAP_ROTATE.getAsBoolean();

        int visibleTiles = (int) (DEFAULT_TILE_COUNT * scale);
        int paddingTiles = BASE_PADDING;
        if (!round && rotate) {
            paddingTiles += Mth.ceil(2F * (Math.max(0F, scale - .5F) * 1.75F));
        }
        return new MapConfig(
                position.computeX(graphics.guiWidth(), mapSize),
                position.computeY(graphics.guiHeight(), mapSize),
                mapSize,
                visibleTiles,
                paddingTiles,
                scale,
                round,
                rotate
        );
    }

    private static ScreenRectangle prepareRenderArea(GuiGraphicsExtractor graphics, MapConfig config, float mapRot) {
        int x = config.x;
        int y = config.y;
        int offset = (config.paddingTiles / 2) * (config.size / config.visibleTiles);
        if (!config.round) {
            graphics.enableScissor(config.x, config.y, config.x + config.size, config.y + config.size);
        }
        if (config.rotate) {
            x = 0;
            y = 0;

            float halfSize = config.size / 2F;
            graphics.pose().pushMatrix();
            graphics.pose().translate(config.x + halfSize, config.y + halfSize);
            graphics.pose().rotate((float) Math.toRadians(mapRot));
            graphics.pose().translate(-halfSize, -halfSize);
        }
        return new ScreenRectangle(x - offset, y - offset, config.size + (offset * 2), config.size + (offset * 2));
    }

    private static void teardownRenderArea(GuiGraphicsExtractor graphics, MapConfig config) {
        if (!config.round) {
            graphics.disableScissor();
        }
        if (config.rotate) {
            graphics.pose().popMatrix();
        }
    }

    private static void extractMapBorder(GuiGraphicsExtractor graphics, MapConfig config) {
        int x = config.x - MINIMAP_BORDER_WIDTH;
        int y = config.y - MINIMAP_BORDER_WIDTH;
        int size = config.size + (MINIMAP_BORDER_WIDTH * 2);
        if (config.round) {
            Identifier texture = Utils.id("textures/gui/sprites/square_frame.png");
            // Misuse color multiplier to send radius, border width and texture size to the shader
            int color = ARGB.color(config.size / 2, MINIMAP_BORDER_WIDTH, FRAME_TEX_SIZE, 0);
            graphics.innerBlit(SimpleMinimapRenderPipelines.MINIMAP_BORDER_ROUND, texture, x, x + size, y, y + size, 0F, 1F, 0F, 1F, color);
        } else {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SQUARE_FRAME, x, y, size, size);
        }
    }

    private static void extractMapDecorations(GuiGraphicsExtractor graphics, MapConfig config, float mapRot) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(config.x + (config.size / 2F), config.y + (config.size / 2F));
        float yRot = 180F;
        if (!config.rotate) {
            yRot -= mapRot;
        }
        graphics.pose().rotate((float) Math.toRadians(yRot));
        graphics.pose().translate(-4.5F, -4F);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PLAYER_MARKER, 0, 0, 8, 8);
        graphics.pose().popMatrix();
    }

    private static MapRenderer getMapRenderer(MapConfig config) {
        if (mapRenderer == null || config.visibleTiles != lastVisibleTileCount || config.paddingTiles != lastPaddingTileCount) {
            if (mapRenderer != null) {
                mapRenderer.close();
            }
            int tileCount = config.getTotalTiles();
            mapRenderer = new MapRenderer("minimap", tileCount, tileCount, config.paddingTiles);
            lastVisibleTileCount = tileCount;
            lastPaddingTileCount = config.paddingTiles;
        }
        return mapRenderer;
    }

    public static void reset() {
        if (mapRenderer != null) {
            mapRenderer.close();
            mapRenderer = null;
        }
    }

    private record MapConfig(
            int x,
            int y,
            int size,
            int visibleTiles,
            int paddingTiles,
            float scale,
            boolean round,
            boolean rotate
    ) {
        int getTotalTiles() {
            return visibleTiles + paddingTiles;
        }
    }
}
