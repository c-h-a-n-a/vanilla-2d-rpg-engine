package game.tile;

public enum TileType {

    GRASS(false),
    EARTH(false),

    TREE(true),
    ROCK(true),

    DUNGEON_DIRT(false),
    DUNGEON_WALL_UP(true),
    DUNGEON_WALL_DOWN(true),
    DUNGEON_WALL_LEFT(true),
    DUNGEON_WALL_RIGHT(true),
    DUNGEON_WALL_LEFT_UP(true),
    DUNGEON_WALL_RIGHT_UP(true),
    DUNGEON_WALL_LEFT_DOWN(true),
    DUNGEON_WALL_RIGHT_DOWN(true),
    DUNGEON_WALL_LEFT_EDGE(true),
    DUNGEON_WALL_RIGHT_EDGE(true);

    private final boolean solid;

    TileType(boolean solid) {
        this.solid = solid;
    }

    public boolean isSolid() {
        return solid;
    }
}