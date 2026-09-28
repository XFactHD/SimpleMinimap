package io.github.xfacthd.simpleminimap.data;

import io.github.xfacthd.simpleminimap.util.Utils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.jspecify.annotations.Nullable;

import java.io.Closeable;
import java.util.Objects;

public final class MapChunkStorage implements Closeable {
    @Nullable
    private static MapChunkStorage activeStorage = null;

    private final ClientLevel level;
    private final Long2ObjectMap<MapChunk> chunks = new Long2ObjectOpenHashMap<>();
    private boolean autoTrim = true;

    private MapChunkStorage(ClientLevel level) {
        this.level = level;
    }

    public static MapChunkStorage get() {
        return Objects.requireNonNull(activeStorage, "No storage active");
    }

    public void forEachInRange(long minChunk, long maxChunk, ChunkConsumer consumer) {
        Utils.forEachInRange(minChunk, maxChunk, chunkPos -> {
            MapChunk chunk = get(chunkPos);
            if (chunk != null) {
                consumer.accept(chunkPos, chunk);
            }
        });
    }

    @SuppressWarnings("DataFlowIssue")
    public @Nullable MapChunk get(long chunkPos) {
        return chunks.get(chunkPos);
    }

    public @Nullable MapChunk getOrLoad(long chunkPos) {
        MapChunk chunk = get(chunkPos);
        if (chunk == null) {
            // TODO: queue loading from disk
        }
        return chunk;
    }

    public void setAutoTrim(boolean autoTrim) {
        this.autoTrim = autoTrim;
    }

    private void loadAroundPlayer(LocalPlayer player) {
        // TODO: queue loading from disk of chunks immediately around the player
    }

    private void trim() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            // TODO: trim storage to only contain chunks within the player's view distance
        }
    }

    @SuppressWarnings("ConstantValue")
    void submitChunkSection(MapChunkSection section) {
        MapChunk prevChunk = chunks.get(section.chunkPos());
        if (prevChunk == null) {
            chunks.put(section.chunkPos(), section.toChunk());
        } else if (prevChunk.dispatchTime.isOutdated(section)) {
            chunks.put(section.chunkPos(), section.toChunk(prevChunk));
        }
    }

    @Override
    public void close() { }

    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ClientLevel level) {
            activeStorage = new MapChunkStorage(level);
        }
    }

    public static void onPlayerConnect(ClientPlayerNetworkEvent.LoggingIn event) {
        if (activeStorage != null) {
            activeStorage.loadAroundPlayer(event.getPlayer());
        }
    }

    public static void onClientTickEnd(ClientTickEvent.Post ignoredEvent) {
        if (activeStorage != null && activeStorage.autoTrim) {
            activeStorage.trim();
        }
    }

    public static void onLevelUnload(LevelEvent.Unload event) {
        if (activeStorage != null && event.getLevel() == activeStorage.level) {
            MapChunkGenerator.cancelTasks();
            activeStorage.close();
            activeStorage = null;
        }
    }

    @FunctionalInterface
    public interface ChunkConsumer {
        void accept(long chunkPos, MapChunk chunk);
    }
}
