package io.github.xfacthd.simpleminimap.renderer;

import com.mojang.logging.LogUtils;
import io.github.xfacthd.pnj.api.PNJ;
import io.github.xfacthd.pnj.api.data.Image;
import io.github.xfacthd.simpleminimap.data.MapChunk;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.material.MapColor;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class MapChunkImage {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean OVERLAY_BORDER = false;

    final int[] pixels;

    private MapChunkImage(int[] pixels) {
        this.pixels = pixels;
    }

    public void save(Path path, long chunkPos) {
        Image image = Image.fromPackedPixels(MapChunk.CHUNK_SIZE, MapChunk.CHUNK_SIZE, pixels, false, true);
        try {
            PNJ.encode(path, image, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            LOGGER.error("Failed to save chunk image for chunk {},{}:", ChunkPos.getX(chunkPos), ChunkPos.getZ(chunkPos), e);
        }
    }

    public static @Nullable MapChunkImage load(Path path, long chunkPos) {
        try {
            Image image = PNJ.decode(path);
            if (image.width() == MapChunk.CHUNK_SIZE && image.height() == MapChunk.CHUNK_SIZE) {
                return new MapChunkImage(image.toPackedPixels(false, true));
            }
            return null;
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            LOGGER.error("Failed to load chunk image for chunk {}, {}:", ChunkPos.getX(chunkPos), ChunkPos.getZ(chunkPos), e);
            return null;
        }
    }

    public static MapChunkImage generate(MapChunk chunk) {
        int[] pixels = newPixelArray();
        for (int z = 0; z < MapChunk.CHUNK_SIZE; z++) {
            for (int x = 0; x < MapChunk.CHUNK_SIZE; x++) {
                MapColor.Brightness brightness = computeBrightness(chunk, x, z);
                int argbColor = chunk.getColor(x, z).calculateARGBColor(brightness);
                pixels[z * MapChunk.CHUNK_SIZE + x] = ARGB.toABGR(argbColor);

                if (OVERLAY_BORDER) {
                    boolean xNotBorder = x > 0 && x < 15;
                    boolean zNotBorder = z > 0 && z < 15;
                    if (!(xNotBorder && zNotBorder)) {
                        pixels[z * MapChunk.CHUNK_SIZE + x] = ARGB.toABGR(0xFFFF0000);
                    }
                }
            }
        }
        return new MapChunkImage(pixels);
    }

    private static MapColor.Brightness computeBrightness(MapChunk chunk, int x, int z) {
        int y = chunk.getHeight(x, z);
        if (chunk.getHeight(x, z - 1) < y || chunk.getHeight(x - 1, z) < y) {
            return MapColor.Brightness.HIGH;
        }
        if (chunk.getHeight(x, z + 1) < y || chunk.getHeight(x + 1, z) < y) {
            return MapColor.Brightness.LOW;
        }
        return MapColor.Brightness.NORMAL;
    }

    private static int[] newPixelArray() {
        return new int[MapChunk.COLOR_COUNT];
    }
}
