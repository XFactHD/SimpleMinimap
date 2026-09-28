package io.github.xfacthd.simpleminimap.renderer;

import io.github.xfacthd.simpleminimap.data.MapChunkStorage;
import it.unimi.dsi.fastutil.longs.Long2LongMap;

public interface MapChunkProvider {
    MapChunkProvider DEFAULT = MapRenderer::forAllModifiedChunks;

    void update(MapChunkStorage storage, Long2LongMap chunkTimes, long minChunk, long maxChunk, MapChunkStorage.ChunkConsumer consumer);
}
