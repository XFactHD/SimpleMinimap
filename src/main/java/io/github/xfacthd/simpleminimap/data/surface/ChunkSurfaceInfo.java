package io.github.xfacthd.simpleminimap.data.surface;

import io.github.xfacthd.simpleminimap.data.MapChunk;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.util.Util;

import java.util.Arrays;

public record ChunkSurfaceInfo(int[] heights, IntSet sections) {
    static final ChunkSurfaceInfo EMPTY = Util.make(() -> {
        int[] heights = MapChunk.newHeightArray();
        Arrays.fill(heights, Integer.MIN_VALUE);
        return new ChunkSurfaceInfo(heights, IntSet.of());
    });
}
