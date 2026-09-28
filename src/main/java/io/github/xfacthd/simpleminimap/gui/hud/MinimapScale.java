package io.github.xfacthd.simpleminimap.gui.hud;

public enum MinimapScale {
    HALF(.5F),
    DEFAULT(1F),
    DOUBLE(2F),
    ;

    final float value;

    MinimapScale(float value) {
        this.value = value;
    }
}
