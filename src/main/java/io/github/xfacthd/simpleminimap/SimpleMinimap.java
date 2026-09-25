package io.github.xfacthd.simpleminimap;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(value = SimpleMinimap.MOD_ID, dist = Dist.CLIENT)
@SuppressWarnings("UtilityClassWithPublicConstructor")
public final class SimpleMinimap {
    public static final String MOD_ID = "simpleminimap";

    public SimpleMinimap(IEventBus modBus) {
    }
}
