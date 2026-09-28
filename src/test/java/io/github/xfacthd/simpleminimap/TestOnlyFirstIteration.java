package io.github.xfacthd.simpleminimap;

import io.github.xfacthd.simpleminimap.util.Utils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class TestOnlyFirstIteration {
    @Test
    void testOneSubsetOfTwo() {
        testRange(ChunkPos.pack(1, 1), ChunkPos.pack(3, 3), ChunkPos.pack(0, 0), ChunkPos.pack(4, 4), LongSet.of());
    }

    @Test
    void testOneEqualsTwo() {
        testRange(ChunkPos.pack(0, 0), ChunkPos.pack(4, 4), ChunkPos.pack(0, 0), ChunkPos.pack(4, 4), LongSet.of());
    }

    @Test
    void testOneLeftAdjacentTwo() {
        LongSet expected = new LongOpenHashSet();
        Utils.forEachInRange(ChunkPos.pack(0, 0), ChunkPos.pack(4, 4), expected::add);
        testRange(ChunkPos.pack(0, 0), ChunkPos.pack(4, 4), ChunkPos.pack(4, 0), ChunkPos.pack(8, 4), expected);
    }

    @Test
    void testOneLeftSeparateTwo() {
        LongSet expected = new LongOpenHashSet();
        Utils.forEachInRange(ChunkPos.pack(0, 0), ChunkPos.pack(4, 4), expected::add);
        testRange(ChunkPos.pack(0, 0), ChunkPos.pack(4, 4), ChunkPos.pack(5, 0), ChunkPos.pack(9, 4), expected);
    }

    @Test
    void testOneRightAdjacentTwo() {
        LongSet expected = new LongOpenHashSet();
        Utils.forEachInRange(ChunkPos.pack(4, 0), ChunkPos.pack(8, 4), expected::add);
        testRange(ChunkPos.pack(4, 0), ChunkPos.pack(8, 4), ChunkPos.pack(0, 0), ChunkPos.pack(4, 4), expected);
    }

    @Test
    void testOneRightSeparateTwo() {
        LongSet expected = new LongOpenHashSet();
        Utils.forEachInRange(ChunkPos.pack(5, 0), ChunkPos.pack(9, 4), expected::add);
        testRange(ChunkPos.pack(5, 0), ChunkPos.pack(9, 4), ChunkPos.pack(0, 0), ChunkPos.pack(4, 4), expected);
    }

    /* ---- */

    @Test
    void testOneEqualsTwoExceptNegZ() {
        LongSet expected = LongSet.of(ChunkPos.pack(0, 0), ChunkPos.pack(1, 0), ChunkPos.pack(2, 0), ChunkPos.pack(3, 0));
        testRange(ChunkPos.pack(0, 0), ChunkPos.pack(4, 4), ChunkPos.pack(0, 1), ChunkPos.pack(4, 4), expected);
    }

    @Test
    void testOneEqualsTwoExceptPosZ() {
        LongSet expected = LongSet.of(ChunkPos.pack(0, 3), ChunkPos.pack(1, 3), ChunkPos.pack(2, 3), ChunkPos.pack(3, 3));
        testRange(ChunkPos.pack(0, 0), ChunkPos.pack(4, 4), ChunkPos.pack(0, 0), ChunkPos.pack(4, 3), expected);
    }

    @Test
    void testOneEqualsTwoExceptNegX() {
        LongSet expected = LongSet.of(ChunkPos.pack(0, 0), ChunkPos.pack(0, 1), ChunkPos.pack(0, 2), ChunkPos.pack(0, 3));
        testRange(ChunkPos.pack(0, 0), ChunkPos.pack(4, 4), ChunkPos.pack(1, 0), ChunkPos.pack(4, 4), expected);
    }

    @Test
    void testOneEqualsTwoExceptPosX() {
        LongSet expected = LongSet.of(ChunkPos.pack(3, 0), ChunkPos.pack(3, 1), ChunkPos.pack(3, 2), ChunkPos.pack(3, 3));
        testRange(ChunkPos.pack(0, 0), ChunkPos.pack(4, 4), ChunkPos.pack(0, 0), ChunkPos.pack(3, 4), expected);
    }

    @Test
    void testOneSmallerThanTwoExceptNegZ() {
        LongSet expected = LongSet.of(ChunkPos.pack(1, 0), ChunkPos.pack(2, 0));
        testRange(ChunkPos.pack(1, 0), ChunkPos.pack(3, 3), ChunkPos.pack(0, 1), ChunkPos.pack(4, 4), expected);
    }

    @Test
    void testOneSmallerThanTwoExceptPosZ() {
        LongSet expected = LongSet.of(ChunkPos.pack(1, 3), ChunkPos.pack(2, 3));
        testRange(ChunkPos.pack(1, 1), ChunkPos.pack(3, 4), ChunkPos.pack(0, 0), ChunkPos.pack(4, 3), expected);
    }

    @Test
    void testOneSmallerThanTwoExceptNegX() {
        LongSet expected = LongSet.of(ChunkPos.pack(0, 1), ChunkPos.pack(0, 2));
        testRange(ChunkPos.pack(0, 1), ChunkPos.pack(3, 3), ChunkPos.pack(1, 0), ChunkPos.pack(4, 4), expected);
    }

    @Test
    void testOneSmallerThanTwoExceptPosX() {
        LongSet expected = LongSet.of(ChunkPos.pack(3, 1), ChunkPos.pack(3, 2));
        testRange(ChunkPos.pack(1, 1), ChunkPos.pack(4, 3), ChunkPos.pack(0, 0), ChunkPos.pack(3, 4), expected);
    }

    /* ---- */

    @Test
    void testOneEqualsTwoExceptZ() {
        LongSet expected = LongSet.of(
                ChunkPos.pack(0, 0), ChunkPos.pack(1, 0), ChunkPos.pack(2, 0), ChunkPos.pack(3, 0),
                ChunkPos.pack(0, 3), ChunkPos.pack(1, 3), ChunkPos.pack(2, 3), ChunkPos.pack(3, 3)
        );
        testRange(ChunkPos.pack(0, 0), ChunkPos.pack(4, 4), ChunkPos.pack(0, 1), ChunkPos.pack(4, 3), expected);
    }

    @Test
    void testOneEqualsTwoExceptX() {
        LongSet expected = LongSet.of(
                ChunkPos.pack(0, 0), ChunkPos.pack(0, 1), ChunkPos.pack(0, 2), ChunkPos.pack(0, 3),
                ChunkPos.pack(3, 0), ChunkPos.pack(3, 1), ChunkPos.pack(3, 2), ChunkPos.pack(3, 3)
        );
        testRange(ChunkPos.pack(0, 0), ChunkPos.pack(4, 4), ChunkPos.pack(1, 0), ChunkPos.pack(3, 4), expected);
    }

    /* ---- */

    @Test
    void testOneEqualsTwoExceptNegXNegZ() {
        LongSet expected = LongSet.of(
                ChunkPos.pack(0, 0),
                ChunkPos.pack(1, 0), ChunkPos.pack(2, 0), ChunkPos.pack(3, 0),
                ChunkPos.pack(0, 1), ChunkPos.pack(0, 2), ChunkPos.pack(0, 3)
        );
        testRange(ChunkPos.pack(0, 0), ChunkPos.pack(4, 4), ChunkPos.pack(1, 1), ChunkPos.pack(4, 4), expected);
    }

    @Test
    void testOneEqualsTwoExceptNegXPosZ() {
        LongSet expected = LongSet.of(
                ChunkPos.pack(0, 3),
                ChunkPos.pack(1, 3), ChunkPos.pack(2, 3), ChunkPos.pack(3, 3),
                ChunkPos.pack(0, 0), ChunkPos.pack(0, 1), ChunkPos.pack(0, 2)
        );
        testRange(ChunkPos.pack(0, 0), ChunkPos.pack(4, 4), ChunkPos.pack(1, 0), ChunkPos.pack(4, 3), expected);
    }

    @Test
    void testOneEqualsTwoExceptPosXNegZ() {
        LongSet expected = LongSet.of(
                ChunkPos.pack(3, 0),
                ChunkPos.pack(0, 0), ChunkPos.pack(1, 0), ChunkPos.pack(2, 0),
                ChunkPos.pack(3, 1), ChunkPos.pack(3, 2), ChunkPos.pack(3, 3)
        );
        testRange(ChunkPos.pack(0, 0), ChunkPos.pack(4, 4), ChunkPos.pack(0, 1), ChunkPos.pack(3, 4), expected);
    }

    @Test
    void testOneEqualsTwoExceptPosXPosZ() {
        LongSet expected = LongSet.of(
                ChunkPos.pack(3, 3),
                ChunkPos.pack(0, 3), ChunkPos.pack(1, 3), ChunkPos.pack(2, 3),
                ChunkPos.pack(3, 0), ChunkPos.pack(3, 1), ChunkPos.pack(3, 2)
        );
        testRange(ChunkPos.pack(0, 0), ChunkPos.pack(4, 4), ChunkPos.pack(0, 0), ChunkPos.pack(3, 3), expected);
    }

    /* ---- */

    @Test
    void testOneSupersetOfTwoExceptNegZ() {
        LongSet expected = LongSet.of(
                ChunkPos.pack(0, 0), ChunkPos.pack(0, 1), ChunkPos.pack(0, 2), ChunkPos.pack(0, 3),
                ChunkPos.pack(3, 0), ChunkPos.pack(3, 1), ChunkPos.pack(3, 2), ChunkPos.pack(3, 3),
                ChunkPos.pack(1, 3), ChunkPos.pack(2, 3)
        );
        testRange(ChunkPos.pack(0, 0), ChunkPos.pack(4, 4), ChunkPos.pack(1, 0), ChunkPos.pack(3, 3), expected);
    }

    @Test
    void testOneSupersetOfTwoExceptPosZ() {
        LongSet expected = LongSet.of(
                ChunkPos.pack(0, 0), ChunkPos.pack(0, 1), ChunkPos.pack(0, 2), ChunkPos.pack(0, 3),
                ChunkPos.pack(3, 0), ChunkPos.pack(3, 1), ChunkPos.pack(3, 2), ChunkPos.pack(3, 3),
                ChunkPos.pack(1, 0), ChunkPos.pack(2, 0)
        );
        testRange(ChunkPos.pack(0, 0), ChunkPos.pack(4, 4), ChunkPos.pack(1, 1), ChunkPos.pack(3, 4), expected);
    }

    @Test
    void testOneSupersetOfTwoExceptNegX() {
        LongSet expected = LongSet.of(
                ChunkPos.pack(0, 0), ChunkPos.pack(1, 0), ChunkPos.pack(2, 0), ChunkPos.pack(3, 0),
                ChunkPos.pack(0, 3), ChunkPos.pack(1, 3), ChunkPos.pack(2, 3), ChunkPos.pack(3, 3),
                ChunkPos.pack(3, 1), ChunkPos.pack(3, 2)
        );
        testRange(ChunkPos.pack(0, 0), ChunkPos.pack(4, 4), ChunkPos.pack(0, 1), ChunkPos.pack(3, 3), expected);
    }

    @Test
    void testOneSupersetOfTwoExceptPosX() {
        LongSet expected = LongSet.of(
                ChunkPos.pack(0, 0), ChunkPos.pack(1, 0), ChunkPos.pack(2, 0), ChunkPos.pack(3, 0),
                ChunkPos.pack(0, 3), ChunkPos.pack(1, 3), ChunkPos.pack(2, 3), ChunkPos.pack(3, 3),
                ChunkPos.pack(0, 1), ChunkPos.pack(0, 2)
        );
        testRange(ChunkPos.pack(0, 0), ChunkPos.pack(4, 4), ChunkPos.pack(1, 1), ChunkPos.pack(4, 3), expected);
    }

    /* ---- */

    @Test
    void testOneSupersetOfTwo() {
        LongSet expected = LongSet.of(
                ChunkPos.pack(0, 0), ChunkPos.pack(1, 0), ChunkPos.pack(2, 0), ChunkPos.pack(3, 0),
                ChunkPos.pack(0, 3), ChunkPos.pack(1, 3), ChunkPos.pack(2, 3), ChunkPos.pack(3, 3),
                ChunkPos.pack(0, 1), ChunkPos.pack(0, 2),
                ChunkPos.pack(3, 1), ChunkPos.pack(3, 2)
        );
        testRange(ChunkPos.pack(0, 0), ChunkPos.pack(4, 4), ChunkPos.pack(1, 1), ChunkPos.pack(3, 3), expected);
    }

    /* ---- */

    private static void testRange(long minChunkOne, long maxChunkOne, long minChunkTwo, long maxChunkTwo, LongSet expected) {
        LongSet result = new LongOpenHashSet();
        Utils.forEachInRangeOnlyFirst(minChunkOne, maxChunkOne, minChunkTwo, maxChunkTwo, key ->
                Assertions.assertTrue(result.add(key), "Duplicate encounter of " + ChunkPos.unpack(key))
        );
        Assertions.assertEquals(expected, result, () -> {
            String message = "Computed positions do not match expected\n";
            message += "Expected:\n";
            message += printGrid(minChunkOne, maxChunkOne, minChunkTwo, maxChunkTwo, expected);
            message += "Actual:\n";
            message += printGrid(minChunkOne, maxChunkOne, minChunkTwo, maxChunkTwo, result);
            return message;
        });
    }

    private static String printGrid(long minChunkOne, long maxChunkOne, long minChunkTwo, long maxChunkTwo, LongSet values) {
        LongSet rangeOne = new LongOpenHashSet();
        LongSet rangeTwo = new LongOpenHashSet();
        Utils.forEachInRange(minChunkOne, maxChunkOne, rangeOne::add);
        Utils.forEachInRange(minChunkTwo, maxChunkTwo, rangeTwo::add);

        int minX = Math.min(ChunkPos.getX(minChunkOne), ChunkPos.getX(minChunkTwo));
        int minZ = Math.min(ChunkPos.getZ(minChunkOne), ChunkPos.getZ(minChunkTwo));
        int maxX = Math.max(ChunkPos.getX(maxChunkOne), ChunkPos.getX(maxChunkTwo));
        int maxZ = Math.max(ChunkPos.getZ(maxChunkOne), ChunkPos.getZ(maxChunkTwo));

        StringBuilder grid = new StringBuilder();
        for (int z = minZ; z < maxZ; z++) {
            for (int x = minX; x < maxX; x++) {
                long pos = ChunkPos.pack(x, z);
                grid.append(rangeOne.contains(pos) ? "1" : "-");
                grid.append(rangeTwo.contains(pos) ? "2" : "-");
                grid.append(values.contains(pos) ? "R" : "-");
                grid.append(" ");
            }
            grid.append("\n");
        }
        return grid.toString();
    }
}
