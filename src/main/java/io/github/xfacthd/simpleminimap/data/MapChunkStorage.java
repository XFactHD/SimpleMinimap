package io.github.xfacthd.simpleminimap.data;

import com.mojang.logging.LogUtils;
import io.github.xfacthd.simpleminimap.gui.hud.MinimapLayer;
import io.github.xfacthd.simpleminimap.util.ChunkRange;
import io.github.xfacthd.simpleminimap.util.Utils;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.Closeable;
import java.util.Objects;

public final class MapChunkStorage implements Closeable {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    private static MapChunkStorage activeStorage = null;

    private final ClientLevel level;
    private final Long2ObjectMap<MapChunk> chunks = new Long2ObjectOpenHashMap<>();
    private final LongSet viewRangeChunks = new LongOpenHashSet();
    private long lastPlayerChunk = 0L;
    private int lastViewDistance = 0;
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

    public void dropIfOutOfRange(long chunkPos) {
        if (!viewRangeChunks.contains(chunkPos)) {
            chunks.remove(chunkPos);
        }
    }

    public void setAutoTrim(boolean autoTrim) {
        this.autoTrim = autoTrim;
    }

    private void loadAroundPlayer(LocalPlayer player) {
        long playerChunk = ChunkPos.pack(player.blockPosition());
        ChunkRange viewRange = Utils.getViewDistanceRange(playerChunk);
        Utils.forEachInRange(viewRange, viewRangeChunks::add);
        lastPlayerChunk = playerChunk;
        lastViewDistance = Minecraft.getInstance().options.getEffectiveRenderDistance();

        // TODO: queue loading from disk of chunks immediately around the player
    }

    private void updateCenterAndTrim() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        long playerChunk = ChunkPos.pack(player.blockPosition());
        int viewDist = Minecraft.getInstance().options.getEffectiveRenderDistance();
        if (playerChunk == lastPlayerChunk && viewDist == lastViewDistance) {
            return;
        }

        if (autoTrim) {
            ChunkRange oldViewRange = Utils.getViewDistanceRange(lastPlayerChunk, lastViewDistance);
            ChunkRange newViewRange = Utils.getViewDistanceRange(playerChunk, viewDist);
            Utils.forEachInRangeOnlyFirst(oldViewRange, newViewRange, pos -> {
                chunks.remove(pos);
                viewRangeChunks.remove(pos);
            });
            Utils.forEachInRangeOnlyFirst(newViewRange, oldViewRange, viewRangeChunks::add);
        }
        lastPlayerChunk = playerChunk;
        lastViewDistance = viewDist;
    }

    @SuppressWarnings("ConstantValue")
    void submitChunkSection(MapChunkSection section) {
        MapChunk prevChunk = chunks.get(section.chunkPos());
        if (prevChunk == null) {
            chunks.put(section.chunkPos(), section.toChunk());
        } else if (prevChunk.dispatchTime.isOutdated(section)) {
            chunks.put(section.chunkPos(), section.toChunk(prevChunk));
        }
        // TODO: queue for saving to disk
    }

    @Override
    public void close() { }

    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ClientLevel level) {
            if (activeStorage != null) {
                LOGGER.warn("Encountered multiple active client levels, discarding secondary");
                return;
            }

            activeStorage = new MapChunkStorage(level);
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                activeStorage.loadAroundPlayer(player);
            }
        }
    }

    public static void onPlayerConnect(ClientPlayerNetworkEvent.LoggingIn event) {
        if (activeStorage != null) {
            activeStorage.loadAroundPlayer(event.getPlayer());
        }
    }

    public static void onClientTickEnd(ClientTickEvent.Post ignoredEvent) {
        if (activeStorage != null) {
            activeStorage.updateCenterAndTrim();
        }
    }

    public static void onLevelUnload(LevelEvent.Unload event) {
        if (activeStorage != null && event.getLevel() == activeStorage.level) {
            MapChunkGenerator.cancelTasks();
            MinimapLayer.reset();
            activeStorage.close();
            activeStorage = null;
        }
    }

    @FunctionalInterface
    public interface ChunkConsumer {
        void accept(long chunkPos, MapChunk chunk);
    }
}
