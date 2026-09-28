package io.github.xfacthd.simpleminimap.renderer;

import io.github.xfacthd.simpleminimap.data.MapChunk;
import io.github.xfacthd.simpleminimap.data.MapChunkStorage;
import io.github.xfacthd.simpleminimap.util.Utils;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;

import java.io.Closeable;

public final class MapRenderer implements Closeable {
    private final int tilesWidth;
    private final int tilesHeight;
    private final MapAggregateTexture aggregateTexture;
    private final Long2LongMap chunkTimes = new Long2LongOpenHashMap();
    private long lastMinChunk = 0L;
    private long lastMaxChunk = 0L;

    public MapRenderer(String name, int tilesWidth, int tilesHeight) {
        this.tilesWidth = tilesWidth;
        this.tilesHeight = tilesHeight;
        String debugLabel = "aggregate/" + name;
        this.aggregateTexture = new MapAggregateTexture(debugLabel, this.tilesWidth, this.tilesHeight);
        Minecraft.getInstance().getTextureManager().register(Utils.id(debugLabel), aggregateTexture);
    }

    public void extract(GuiGraphicsExtractor graphics, MapChunkProvider chunkProvider, MapBlitter blitter, long minChunk, long maxChunk, int blockOffX, int blockOffZ, ScreenRectangle rect) {
        cleanupChunkTimeCache(minChunk, maxChunk);
        updateAggregateTexture(chunkProvider, minChunk, maxChunk);

        float texWidth = aggregateTexture.getWidth();
        float texHeight = aggregateTexture.getHeight();
        int minX = ChunkPos.getX(minChunk);
        int minZ = ChunkPos.getZ(minChunk);
        int rangeWidth = (ChunkPos.getX(maxChunk) - minX) * MapChunk.CHUNK_SIZE;
        int rangeHeight = (ChunkPos.getZ(maxChunk) - minZ) * MapChunk.CHUNK_SIZE;
        float u0 = ((Mth.positiveModulo(minX, tilesWidth) * MapChunk.CHUNK_SIZE) + blockOffX) / texWidth;
        float v0 = ((Mth.positiveModulo(minZ, tilesHeight) * MapChunk.CHUNK_SIZE) + blockOffZ) / texHeight;
        float u1 = u0 + (rangeWidth / texWidth);
        float v1 = v0 + (rangeHeight / texHeight);
        blitter.blit(graphics, aggregateTexture.getTextureView(), aggregateTexture.getSampler(), rect, u0, u1, v0, v1);
    }

    private void cleanupChunkTimeCache(long minChunk, long maxChunk) {
        if (minChunk != lastMinChunk || maxChunk != lastMaxChunk) {
            Utils.forEachInRangeOnlyFirst(lastMinChunk, lastMaxChunk, minChunk, maxChunk, chunkTimes::remove);

            lastMinChunk = minChunk;
            lastMaxChunk = maxChunk;
        }
    }

    private void updateAggregateTexture(MapChunkProvider chunkProvider, long minChunk, long maxChunk) {
        boolean[] changed = new boolean[1];
        chunkProvider.update(MapChunkStorage.get(), chunkTimes, minChunk, maxChunk, (chunkPos, chunk) -> {
            chunkTimes.put(chunkPos, chunk.getLastUpdateTime());
            int tileX = Mth.positiveModulo(ChunkPos.getX(chunkPos), tilesWidth);
            int tileY = Mth.positiveModulo(ChunkPos.getZ(chunkPos), tilesHeight);
            aggregateTexture.insertChunk(tileX, tileY, chunk.getImage());
            changed[0] = true;
        });
        if (changed[0]) {
            aggregateTexture.upload();
        }
    }

    static void forAllModifiedChunks(MapChunkStorage storage, Long2LongMap chunkTimes, long minChunk, long maxChunk, MapChunkStorage.ChunkConsumer consumer) {
        storage.forEachInRange(minChunk, maxChunk, (chunkPos, chunk) -> {
            if (chunkTimes.get(chunkPos) < chunk.getLastUpdateTime()) {
                consumer.accept(chunkPos, chunk);
            }
        });
    }

    @Override
    public void close() {
        aggregateTexture.close();
    }
}
