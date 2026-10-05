package io.github.xfacthd.simpleminimap.data;

import io.github.xfacthd.simpleminimap.data.surface.ChunkSurfaceInfo;
import io.github.xfacthd.simpleminimap.data.surface.SurfaceReader;
import io.github.xfacthd.simpleminimap.util.ThrottledTaskStore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Util;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.Zone;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;

import java.util.BitSet;
import java.util.concurrent.Executor;

public final class MapChunkGenerator {
    private static final ThrottledTaskStore TASK_STORE = new ThrottledTaskStore();
    private static final Executor EXECUTOR = Util.backgroundExecutor().forName("SimpleMinimap-chunk_compiler");

    public static void onAddSectionGeometry(AddSectionGeometryEvent event) {
        BlockPos origin = event.getSectionOrigin();
        int chunkX = SectionPos.blockToSectionCoord(origin.getX());
        int chunkZ = SectionPos.blockToSectionCoord(origin.getZ());
        ChunkSurfaceInfo surfaceInfo = SurfaceReader.read(event.getLevel(), chunkX, chunkZ);
        if (surfaceInfo.sections().contains(SectionPos.blockToSectionCoord(origin.getY()))) {
            BlockPos safeOrigin = origin.immutable();
            long dispatchTime = System.nanoTime();
            event.addRenderer(ctx -> generateMapChunk(ctx.getRegion(), safeOrigin, surfaceInfo.heights(), dispatchTime));
        }
    }

    static void cancelTasks() {
        TASK_STORE.cancelTasks();
    }

    private static void generateMapChunk(BlockAndTintGetter level, BlockPos origin, int[] heights, long dispatchTime) {
        TASK_STORE.addTask(future -> future
                .thenApplyAsync(_ -> compileMapChunk(level, heights, origin, dispatchTime), EXECUTOR)
                .thenAcceptAsync(MapChunkStorage.get()::submitChunkSection, Minecraft.getInstance())
        );
    }

    private static MapChunkSection compileMapChunk(BlockAndTintGetter level, int[] heights, BlockPos origin, long dispatchTime) {
        try (Zone ignored = Profiler.get().zone("SimpleMinimap - Compile Map Chunk")) {
            BitSet mask = new BitSet(MapChunk.COLOR_COUNT);
            MapColor[] colors = new MapColor[MapChunk.COLOR_COUNT];
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            int sectionMinY = origin.getY();
            int sectionMaxY = sectionMinY + MapChunk.CHUNK_SIZE;
            for (int z = 0; z < MapChunk.CHUNK_SIZE; z++) {
                for (int x = 0; x < MapChunk.CHUNK_SIZE; x++) {
                    int y = heights[MapChunk.paddedIndex(x, z)];
                    int colIdx = z * MapChunk.CHUNK_SIZE + x;
                    MapColor mapColor;
                    if (y >= sectionMinY && y < sectionMaxY) {
                        pos.setWithOffset(origin, x, y - origin.getY(), z);
                        mapColor = level.getBlockState(pos).getMapColor(level, pos);
                        mask.set(colIdx);
                    } else {
                        mapColor = MapColor.NONE;
                    }
                    colors[colIdx] = mapColor;
                }
            }
            int chunkX = SectionPos.blockToSectionCoord(origin.getX());
            int chunkZ = SectionPos.blockToSectionCoord(origin.getZ());
            if (mask.nextClearBit(0) >= MapChunk.COLOR_COUNT) {
                mask = null;
            }
            return new MapChunkSection(chunkX, chunkZ, sectionMinY, heights, colors, mask, dispatchTime);
        }
    }

    private MapChunkGenerator() { }
}
