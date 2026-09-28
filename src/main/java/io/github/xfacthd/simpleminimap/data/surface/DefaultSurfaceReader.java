package io.github.xfacthd.simpleminimap.data.surface;

import io.github.xfacthd.simpleminimap.data.MapChunk;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;

final class DefaultSurfaceReader extends SurfaceReader {
    static final SurfaceReader INSTANCE = new DefaultSurfaceReader();

    @Override
    ChunkSurfaceInfo readUncached(Level level, long chunkPos) {
        int chunkX = ChunkPos.getX(chunkPos);
        int chunkZ = ChunkPos.getZ(chunkPos);
        Heightmap heightmap = getHeigthmap(level, chunkX, chunkZ);
        if (heightmap == null) {
            return ChunkSurfaceInfo.EMPTY;
        }

        int[] heights = MapChunk.newHeightArray();
        IntSet sections = new IntOpenHashSet();
        for (int z = 0; z < MapChunk.CHUNK_SIZE; z++) {
            for (int x = 0; x < MapChunk.CHUNK_SIZE; x++) {
                int y = heightmap.getHighestTaken(x, z);
                heights[MapChunk.paddedIndex(x, z)] = y;
                sections.add(SectionPos.blockToSectionCoord(y));
            }
        }

        Heightmap heightmapNX = getHeigthmap(level, chunkX - 1, chunkZ);
        Heightmap heightmapPX = getHeigthmap(level, chunkX + 1, chunkZ);
        for (int z = 0; z < MapChunk.CHUNK_SIZE; z++) {
            heights[MapChunk.paddedIndex(-1, z)] = heightmapNX != null ? heightmapNX.getHighestTaken(15, z) : Integer.MIN_VALUE;
            heights[MapChunk.paddedIndex(16, z)] = heightmapPX != null ? heightmapPX.getHighestTaken(0, z) : Integer.MIN_VALUE;
        }
        Heightmap heightmapNZ = getHeigthmap(level, chunkX, chunkZ - 1);
        Heightmap heightmapPZ = getHeigthmap(level, chunkX, chunkZ + 1);
        for (int x = 0; x < MapChunk.CHUNK_SIZE; x++) {
            heights[MapChunk.paddedIndex(x, -1)] = heightmapNZ != null ? heightmapNZ.getHighestTaken(x, 15) : Integer.MIN_VALUE;
            heights[MapChunk.paddedIndex(x, 16)] = heightmapPZ != null ? heightmapPZ.getHighestTaken(x, 0) : Integer.MIN_VALUE;
        }

        return new ChunkSurfaceInfo(heights, sections);
    }
}
