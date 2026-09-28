package io.github.xfacthd.simpleminimap.data.surface;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract sealed class SurfaceReader permits DefaultSurfaceReader {
    private static final List<SurfaceReader> READERS = new ArrayList<>();
    private static final Heightmap.Types HEIGHTMAP_TYPE = Heightmap.Types.MOTION_BLOCKING;

    private final Long2ObjectMap<ChunkSurfaceInfo> surfaceData = new Long2ObjectOpenHashMap<>();

    SurfaceReader() {
        READERS.add(this);
    }

    public static ChunkSurfaceInfo read(Level level, int chunkX, int chunkZ) {
        // TODO: handle dimensions with a ceiling like the Nether (level.dimensionType().hasCeiling())
        return DefaultSurfaceReader.INSTANCE.read(level, ChunkPos.pack(chunkX, chunkZ));
    }

    @SuppressWarnings("ConstantValue")
    private ChunkSurfaceInfo read(Level level, long chunkPos) {
        ChunkSurfaceInfo info = this.surfaceData.get(chunkPos);
        if (info == null) {
            info = readUncached(level, chunkPos);
        }
        return info;
    }

    abstract ChunkSurfaceInfo readUncached(Level level, long chunkPos);

    static @Nullable Heightmap getHeigthmap(Level level, int chunkX, int chunkZ) {
        LevelChunk chunk = level.getChunk(chunkX, chunkZ);
        if (!chunk.isEmpty() && chunk.hasPrimedHeightmap(HEIGHTMAP_TYPE)) {
            return chunk.getOrCreateHeightmapUnprimed(HEIGHTMAP_TYPE);
        }
        return null;
    }

    private void reset() {
        surfaceData.clear();
    }

    public static void onClientTickEnd(ClientTickEvent.Post ignoredEvent) {
        READERS.forEach(SurfaceReader::reset);
    }
}
