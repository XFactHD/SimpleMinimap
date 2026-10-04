package io.github.xfacthd.simpleminimap.util;

import io.github.xfacthd.simpleminimap.SimpleMinimap;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;

import java.util.function.LongConsumer;

public final class Utils {
    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(SimpleMinimap.MOD_ID, path);
    }

    public static ChunkRange getViewDistanceRange(long centerChunk) {
        return getViewDistanceRange(centerChunk, Minecraft.getInstance().options.getEffectiveRenderDistance());
    }

    public static ChunkRange getViewDistanceRange(long centerChunk, int viewDist) {

        int playerChunkX = ChunkPos.getX(centerChunk);
        int playerChunkZ = ChunkPos.getZ(centerChunk);
        return new ChunkRange(
                ChunkPos.pack(playerChunkX - viewDist, playerChunkZ - viewDist),
                ChunkPos.pack(playerChunkX + viewDist + 1, playerChunkZ + viewDist + 1)
        );
    }

    public static void forEachInRange(ChunkRange range, LongConsumer consumer) {
        forEachInRange(range.minChunk(), range.maxChunk(), consumer);
    }

    public static void forEachInRange(long minChunk, long maxChunk, LongConsumer consumer) {
        int minX = ChunkPos.getX(minChunk);
        int minZ = ChunkPos.getZ(minChunk);
        int maxX = ChunkPos.getX(maxChunk);
        int maxZ = ChunkPos.getZ(maxChunk);
        for (int z = minZ; z < maxZ; z++) {
            for (int x = minX; x < maxX; x++) {
                consumer.accept(ChunkPos.pack(x, z));
            }
        }
    }

    public static void forEachInRangeOnlyFirst(ChunkRange rangeOne, ChunkRange rangeTwo, LongConsumer consumer) {
        forEachInRangeOnlyFirst(rangeOne.minChunk(), rangeOne.maxChunk(), rangeTwo.minChunk(), rangeTwo.maxChunk(), consumer);
    }

    /// Iterates over all positions present in the first range but absent from the second range
    public static void forEachInRangeOnlyFirst(long minChunkOne, long maxChunkOne, long minChunkTwo, long maxChunkTwo, LongConsumer consumer) {
        int minOneX = ChunkPos.getX(minChunkOne);
        int minOneZ = ChunkPos.getZ(minChunkOne);
        int maxOneX = ChunkPos.getX(maxChunkOne);
        int maxOneZ = ChunkPos.getZ(maxChunkOne);
        int minTwoX = ChunkPos.getX(minChunkTwo);
        int minTwoZ = ChunkPos.getZ(minChunkTwo);
        int maxTwoX = ChunkPos.getX(maxChunkTwo);
        int maxTwoZ = ChunkPos.getZ(maxChunkTwo);

        for (int x = minOneX; x < Math.min(maxOneX, minTwoX); x++) {
            for (int z = minOneZ; z < maxOneZ; z++) {
                consumer.accept(ChunkPos.pack(x, z));
            }
        }
        for (int x = Math.max(minOneX, maxTwoX); x < maxOneX; x++) {
            for (int z = minOneZ; z < maxOneZ; z++) {
                consumer.accept(ChunkPos.pack(x, z));
            }
        }

        int minX = Math.max(minOneX, minTwoX);
        int maxX = Math.min(maxOneX, maxTwoX);
        for (int z = minOneZ; z < Math.min(maxOneZ, minTwoZ); z++) {
            for (int x = minX; x < maxX; x++) {
                consumer.accept(ChunkPos.pack(x, z));
            }
        }
        for (int z = Math.max(minOneZ, maxTwoZ); z < maxOneZ; z++) {
            for (int x = minX; x < maxX; x++) {
                consumer.accept(ChunkPos.pack(x, z));
            }
        }
    }

    private Utils() { }
}
