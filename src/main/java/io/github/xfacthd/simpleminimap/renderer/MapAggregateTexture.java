package io.github.xfacthd.simpleminimap.renderer;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuTexture;
import io.github.xfacthd.simpleminimap.data.MapChunk;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.resources.Identifier;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.Locale;
import java.util.function.IntUnaryOperator;

final class MapAggregateTexture extends AbstractTexture implements Dumpable {
    @GpuTexture.Usage
    private static final int USAGE = GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_COPY_SRC | GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_RENDER_ATTACHMENT;

    private final int width;
    private final int height;
    private final ByteBuffer buffer;
    private boolean allocated;

    MapAggregateTexture(String label, int tilesWidth, int tilesHeight) {
        this.width = tilesWidth * MapChunk.CHUNK_SIZE;
        this.height = tilesHeight * MapChunk.CHUNK_SIZE;
        this.texture = RenderSystem.getDevice().createTexture(label, USAGE, GpuFormat.RGBA8_UNORM, width, height, 1, 1);
        this.textureView = RenderSystem.getDevice().createTextureView(texture);
        this.sampler = RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST);
        long size = (long) width * height * Integer.BYTES;
        if (size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "Cannot allocate texture of size %dx%d", width, height));
        }
        this.buffer = MemoryUtil.memCalloc((int) size);
        this.allocated = true;
        upload();
    }

    int getWidth() {
        return width;
    }

    int getHeight() {
        return height;
    }

    void insertChunk(int tileX, int tileY, MapChunkImage image) {
        assertAllocated();

        IntBuffer intBuffer = buffer.asIntBuffer();
        int[] pixels = image.pixels;
        int baseOff = (tileY * MapChunk.CHUNK_SIZE * width) + (tileX * MapChunk.CHUNK_SIZE);
        for (int y = 0; y < MapChunk.CHUNK_SIZE; y++) {
            int srcLineOff = y * MapChunk.CHUNK_SIZE;
            int destPixelOff = baseOff + (y * width);
            intBuffer.put(destPixelOff, pixels, srcLineOff, MapChunk.CHUNK_SIZE);
        }
    }

    void upload() {
        assertAllocated();
        RenderSystem.getDevice().createCommandEncoder().writeToTexture(getTexture(), buffer, 0, 0, 0, 0, width, height);
    }

    @Override
    public void close() {
        super.close();
        if (allocated) {
            allocated = false;
            MemoryUtil.memFree(buffer);
        }
    }

    @Override
    public void dumpContents(Identifier identifier, Path path) {
        assertAllocated();
        TextureUtil.writeAsPNG(path, identifier.toDebugFileName(), getTexture(), 0, IntUnaryOperator.identity());
    }

    private void assertAllocated() {
        Preconditions.checkState(allocated, "Texture not allocated");
    }
}
