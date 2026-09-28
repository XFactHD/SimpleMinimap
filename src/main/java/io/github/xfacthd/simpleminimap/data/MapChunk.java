package io.github.xfacthd.simpleminimap.data;

import io.github.xfacthd.simpleminimap.renderer.MapChunkImage;
import it.unimi.dsi.fastutil.ints.Int2LongMap;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import net.minecraft.world.level.material.MapColor;
import org.jspecify.annotations.Nullable;

public final class MapChunk {
    public static final int CHUNK_SIZE = 16;
    public static final int PADDED_CHUNK_SIZE = CHUNK_SIZE + 2;
    static final int HEIGHT_COUNT = PADDED_CHUNK_SIZE * PADDED_CHUNK_SIZE;
    public static final int COLOR_COUNT = CHUNK_SIZE * CHUNK_SIZE;

    final int[] heights;
    final MapColor[] colors;
    final DispatchTime dispatchTime;
    @Nullable
    private MapChunkImage image;

    MapChunk(int[] heights, MapColor[] colors, DispatchTime dispatchTime) {
        this.heights = heights;
        this.colors = colors;
        this.dispatchTime = dispatchTime;
    }

    public int getHeight(int x, int z) {
        return heights[paddedIndex(x, z)];
    }

    public MapColor getColor(int x, int z) {
        return colors[z * CHUNK_SIZE + x];
    }

    public long getLastUpdateTime() {
        return dispatchTime.getMostRecent();
    }

    public MapChunkImage getImage() {
        if (image == null) {
            image = MapChunkImage.generate(this);
        }
        return image;
    }

    public static int paddedIndex(int x, int z) {
        return (z + 1) * PADDED_CHUNK_SIZE + (x + 1);
    }

    public static int[] newHeightArray() {
        return new int[HEIGHT_COUNT];
    }

    @SuppressWarnings("ClassEscapesDefinedScope")
    sealed interface DispatchTime {
        long getMostRecent();

        boolean isOutdated(MapChunkSection section);

        record Single(long timestamp) implements DispatchTime {
            @Override
            public long getMostRecent() {
                return timestamp;
            }

            @Override
            public boolean isOutdated(MapChunkSection section) {
                return section.dispatchTimestamp() > timestamp;
            }
        }

        record Multiple(Int2LongMap timestamps, long mostRecent) implements DispatchTime {
            Multiple(int sectionY, long timestamp) {
                Int2LongMap timestamps = new Int2LongOpenHashMap();
                timestamps.put(sectionY, timestamp);
                this(timestamps, timestamp);
            }

            @Override
            public long getMostRecent() {
                return mostRecent;
            }

            @Override
            public boolean isOutdated(MapChunkSection section) {
                if (section.isFullCoverage()) {
                    return section.dispatchTimestamp() > mostRecent;
                }
                return section.dispatchTimestamp() > timestamps.get(section.sectionY());
            }

            Multiple withSection(int sectionY, long dispatchTimestamp) {
                Int2LongMap newTimestamps = new Int2LongOpenHashMap(timestamps);
                newTimestamps.put(sectionY, dispatchTimestamp);
                return new Multiple(newTimestamps, Math.max(mostRecent, dispatchTimestamp));
            }
        }
    }
}
