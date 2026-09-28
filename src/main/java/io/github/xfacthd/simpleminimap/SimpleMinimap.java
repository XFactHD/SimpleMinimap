package io.github.xfacthd.simpleminimap;

import io.github.xfacthd.simpleminimap.data.MapChunkGenerator;
import io.github.xfacthd.simpleminimap.data.MapChunkStorage;
import io.github.xfacthd.simpleminimap.data.surface.SurfaceReader;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = SimpleMinimap.MOD_ID, dist = Dist.CLIENT)
@SuppressWarnings("UtilityClassWithPublicConstructor")
public final class SimpleMinimap {
    public static final String MOD_ID = "simpleminimap";

    public SimpleMinimap(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(MapChunkGenerator::onAddSectionGeometry);
        NeoForge.EVENT_BUS.addListener(SurfaceReader::onClientTickEnd);
        NeoForge.EVENT_BUS.addListener(MapChunkStorage::onLevelLoad);
        NeoForge.EVENT_BUS.addListener(MapChunkStorage::onPlayerConnect);
        NeoForge.EVENT_BUS.addListener(MapChunkStorage::onClientTickEnd);
        NeoForge.EVENT_BUS.addListener(MapChunkStorage::onLevelUnload);
    }
}
