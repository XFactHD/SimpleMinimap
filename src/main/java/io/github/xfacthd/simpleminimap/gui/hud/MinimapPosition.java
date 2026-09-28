package io.github.xfacthd.simpleminimap.gui.hud;

public record MinimapPosition(Corner corner, int xOff, int yOff) {
    public int computeX(int screenWidth, int mapSize) {
        int offset = xOff + MinimapLayer.MINIMAP_BORDER_WIDTH;
        return corner.left ? offset : (screenWidth - mapSize - offset);
    }

    public int computeY(int screenHeight, int mapSize) {
        int offset = yOff + MinimapLayer.MINIMAP_BORDER_WIDTH;
        return corner.top ? offset : (screenHeight - mapSize - offset);
    }

    public enum Corner {
        TOP_LEFT(true, true),
        TOP_RIGHT(false, true),
        BOTTOM_LEFT(true, false),
        BOTTOM_RIGHT(false, false);

        private final boolean left;
        private final boolean top;

        Corner(boolean left, boolean top) {
            this.left = left;
            this.top = top;
        }
    }
}
