package game.tile;

public enum TileType {

    GRASS(false),
    EARTH(false),

    TREE(true),
    ROCK(true);

    private final boolean solid;

    TileType(boolean solid) {
        this.solid = solid;
    }

    public boolean isSolid() {
        return solid;
    }
}