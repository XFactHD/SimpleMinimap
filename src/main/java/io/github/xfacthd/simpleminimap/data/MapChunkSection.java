package io.github.xfacthd.simpleminimap.data;

import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.material.MapColor;
import org.jspecify.annotations.Nullable;

import java.util.BitSet;

record MapChunkSection(long chunkPos, int sectionY, int[] heights, MapColor[] colors, @Nullable BitSet columnMask, long dispatchTimestamp) {
    MapChunkSection(int chunkX, int chunkZ, int sectionMinY, int[] heights, MapColor[] colors, @Nullable BitSet columnMask, long dispatchTime) {
        this(ChunkPos.pack(chunkX, chunkZ), SectionPos.blockToSectionCoord(sectionMinY), heights, colors, columnMask, dispatchTime);
    }

    boolean isFullCoverage() {
        return columnMask == null;
    }

    MapChunk toChunk() {
        MapChunk.DispatchTime dispatchTime;
        if (columnMask != null) {
            dispatchTime = new MapChunk.DispatchTime.Multiple(sectionY, dispatchTimestamp);
        } else {
            dispatchTime = new MapChunk.DispatchTime.Single(dispatchTimestamp);
        }
        return new MapChunk(heights, colors, dispatchTime);
    }

    MapChunk toChunk(MapChunk prevChunk) {
        if (columnMask == null) {
            return toChunk();
        }

        int[] newHeights;
        if (dispatchTimestamp > prevChunk.getLastUpdateTime()) {
            newHeights = heights;
        } else {
            newHeights = prevChunk.heights.clone();
        }
        MapColor[] newColors = prevChunk.colors.clone();
        for (int z = 0; z < MapChunk.CHUNK_SIZE; z++) {
            for (int x = 0; x < MapChunk.CHUNK_SIZE; x++) {
                int colIdx = z * MapChunk.CHUNK_SIZE + x;
                if (columnMask.get(colIdx)) {
                    int heightIdx = MapChunk.paddedIndex(x, z);
                    newHeights[heightIdx] = heights[heightIdx];
                    newColors[colIdx] = colors[colIdx];
                }
            }
        }
        MapChunk.DispatchTime dispatchTime = switch (prevChunk.dispatchTime) {
            case MapChunk.DispatchTime.Single _ -> new MapChunk.DispatchTime.Multiple(sectionY, dispatchTimestamp);
            case MapChunk.DispatchTime.Multiple multiple -> multiple.withSection(sectionY, dispatchTimestamp);
        };
        return new MapChunk(newHeights, newColors, dispatchTime);
    }
}
