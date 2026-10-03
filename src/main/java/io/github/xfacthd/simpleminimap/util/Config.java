package io.github.xfacthd.simpleminimap.util;

import io.github.xfacthd.simpleminimap.gui.hud.MinimapLayer;
import io.github.xfacthd.simpleminimap.gui.hud.MinimapPosition;
import io.github.xfacthd.simpleminimap.gui.hud.MinimapScale;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    private static final ModConfigSpec SPEC;

    private static final String KEY_GRID_LINE_WIDTH = "grid_line_width";

    private static final String KEY_MINIMAP_POSITION = "position";
    private static final String KEY_MINIMAP_OFF_X = "offset_x";
    private static final String KEY_MINIMAP_OFF_Y = "offset_y";
    private static final String KEY_MINIMAP_SIZE = "size";
    private static final String KEY_MINIMAP_SCALE = "scale";
    private static final String KEY_MINIMAP_ROUND = "round";
    private static final String KEY_MINIMAP_ROTATE = "rotate";

    public static final ModConfigSpec.IntValue GRID_LINE_WIDTH;

    public static final ModConfigSpec.EnumValue<MinimapPosition.Corner> MINIMAP_POSITION;
    public static final ModConfigSpec.IntValue MINIMAP_OFF_X;
    public static final ModConfigSpec.IntValue MINIMAP_OFF_Y;
    public static final ModConfigSpec.IntValue MINIMAP_SIZE;
    public static final ModConfigSpec.EnumValue<MinimapScale> MINIMAP_SCALE;
    public static final ModConfigSpec.BooleanValue MINIMAP_ROUND;
    public static final ModConfigSpec.BooleanValue MINIMAP_ROTATE;
    private static MinimapPosition minimapPosition = new MinimapPosition(MinimapPosition.Corner.TOP_RIGHT, 10, 10);

    public static void init(IEventBus modBus, ModContainer modContainer) {
        modBus.addListener((ModConfigEvent.Loading event) -> onConfigReloaded(event));
        modBus.addListener((ModConfigEvent.Reloading event) -> onConfigReloaded(event));
        modContainer.registerConfig(ModConfig.Type.CLIENT, SPEC);
    }

    public static MinimapPosition getMinimapPosition() {
        return minimapPosition;
    }

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.translation(translateCategory("general")).push("general");
        GRID_LINE_WIDTH = builder
                .comment(
                        "Specifies the width of chunk grid lines.",
                        "Setting this to zero disables the chunk grid."
                )
                .translation(translateValue(KEY_GRID_LINE_WIDTH))
                .defineInRange(KEY_GRID_LINE_WIDTH, 2, 1, 16);
        builder.pop();

        builder.translation(translateCategory("minimap")).push("minimap");
        MINIMAP_POSITION = builder
                .comment("Controls which corner of the screen the minimap is placed in")
                .translation(translateValue(KEY_MINIMAP_POSITION))
                .defineEnum(KEY_MINIMAP_POSITION, MinimapPosition.Corner.TOP_RIGHT);
        MINIMAP_OFF_X = builder
                .comment("Controls the offset of the minimap from the screen's horizontal edge specified by the position")
                .translation(translateValue(KEY_MINIMAP_OFF_X))
                .defineInRange(KEY_MINIMAP_OFF_X, MinimapLayer.DEFAULT_MAP_OFFSET, MinimapLayer.MIN_MAP_OFFSET, MinimapLayer.MAX_MAP_OFFSET);
        MINIMAP_OFF_Y = builder
                .comment("Controls the offset of the minimap from the screen's vertical edge specified by the position")
                .translation(translateValue(KEY_MINIMAP_OFF_Y))
                .defineInRange(KEY_MINIMAP_OFF_Y, MinimapLayer.DEFAULT_MAP_OFFSET, MinimapLayer.MIN_MAP_OFFSET, MinimapLayer.MAX_MAP_OFFSET);
        MINIMAP_SIZE = builder
                .comment("Controls the size of the minimap content")
                .translation(translateValue(KEY_MINIMAP_SIZE))
                .defineInRange(KEY_MINIMAP_SIZE, MinimapLayer.DEFAULT_MAP_SIZE, MinimapLayer.MIN_MAP_SIZE, MinimapLayer.MAX_MAP_SIZE);
        MINIMAP_SCALE = builder
                .comment("Specifies the scale of the minimap contents. At default scale the minimap will show 8x8 chunks")
                .translation(translateValue(KEY_MINIMAP_SCALE))
                .defineEnum(KEY_MINIMAP_SCALE, MinimapScale.DEFAULT);
        MINIMAP_ROUND = builder
                .comment("Controls whether the minimap is round (true) or square (false)")
                .translation(translateValue(KEY_MINIMAP_ROUND))
                .define(KEY_MINIMAP_ROUND, false);
        MINIMAP_ROTATE = builder
                .comment(
                        "Controls whether the minimap or the player marker rotates",
                        "If true, the minimap rotates around a stationary player marker",
                        "If false, the player marker rotates on a stationary minimap"
                )
                .translation(translateValue(KEY_MINIMAP_ROTATE))
                .define(KEY_MINIMAP_ROTATE, false);
        builder.pop();

        SPEC = builder.build();
    }

    private static String translateCategory(String key) {
        return translate("category." + key);
    }

    private static String translateValue(String key) {
        return translate("value." + key);
    }

    private static String translate(String key) {
        return "config.simpleminimap.client." + key;
    }

    private static void onConfigReloaded(ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            minimapPosition = new MinimapPosition(
                    MINIMAP_POSITION.get(),
                    MINIMAP_OFF_X.getAsInt(),
                    MINIMAP_OFF_Y.getAsInt()
            );
        }
    }

    private Config() { }
}
