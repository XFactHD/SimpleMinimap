package io.github.xfacthd.simpleminimap;

import io.github.xfacthd.simpleminimap.data.MapChunkGenerator;
import io.github.xfacthd.simpleminimap.data.MapChunkStorage;
import io.github.xfacthd.simpleminimap.data.surface.SurfaceReader;
import io.github.xfacthd.simpleminimap.gui.hud.MinimapLayer;
import io.github.xfacthd.simpleminimap.renderer.SimpleMinimapRenderPipelines;
import io.github.xfacthd.simpleminimap.util.Config;
import io.github.xfacthd.simpleminimap.util.Utils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = SimpleMinimap.MOD_ID, dist = Dist.CLIENT)
@SuppressWarnings("UtilityClassWithPublicConstructor")
public final class SimpleMinimap {
    public static final String MOD_ID = "simpleminimap";

    public SimpleMinimap(IEventBus modBus, ModContainer modContainer) {
        Config.init(modBus, modContainer);
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        modBus.addListener(SimpleMinimap::onRegisterGuiLayers);
        modBus.addListener(SimpleMinimapRenderPipelines::onRegisterRenderPipelines);

        NeoForge.EVENT_BUS.addListener(MapChunkGenerator::onAddSectionGeometry);
        NeoForge.EVENT_BUS.addListener(SurfaceReader::onClientTickEnd);
        NeoForge.EVENT_BUS.addListener(MapChunkStorage::onLevelLoad);
        NeoForge.EVENT_BUS.addListener(MapChunkStorage::onPlayerConnect);
        NeoForge.EVENT_BUS.addListener(MapChunkStorage::onClientTickEnd);
        NeoForge.EVENT_BUS.addListener(MapChunkStorage::onLevelUnload);
    }

    private static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(Utils.id("minimap"), new MinimapLayer());
    }
}
