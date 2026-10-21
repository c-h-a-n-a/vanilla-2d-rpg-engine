package game.tile;

/**
 * Every kind of tile in the game: its image and whether it blocks movement.
 * To add a tile, add one line here and drop the PNG in the resources folder.
 */
public enum TileType {

    GRASS(false, "/tiles/grass.png"),
    EARTH(false, "/tiles/earth.png"),

    TREE(true, "/tiles/tree.png"),
    ROCK(true, "/tiles/rock.png"),

    DUNGEON_DIRT(false, "/tiles/dungeon/dungeon_dirt.png"),
    DUNGEON_WALL_UP(true, "/tiles/dungeon/dungeon_wall_up.png"),
    DUNGEON_WALL_DOWN(true, "/tiles/dungeon/dungeon_wall_down.png"),
    DUNGEON_WALL_LEFT(true, "/tiles/dungeon/dungeon_wall_left.png"),
    DUNGEON_WALL_RIGHT(true, "/tiles/dungeon/dungeon_wall_right.png"),
    DUNGEON_WALL_LEFT_UP(true, "/tiles/dungeon/dungeon_wall_up_left.png"),
    DUNGEON_WALL_RIGHT_UP(true, "/tiles/dungeon/dungeon_wall_up_right.png"),
    DUNGEON_WALL_LEFT_DOWN(true, "/tiles/dungeon/dungeon_wall_left_down.png"),
    DUNGEON_WALL_RIGHT_DOWN(true, "/tiles/dungeon/dungeon_wall_right_down.png"),
    DUNGEON_WALL_LEFT_EDGE(true, "/tiles/dungeon/dungeon_wall_left_edge.png"),
    DUNGEON_WALL_RIGHT_EDGE(true, "/tiles/dungeon/dungeon_wall_right_edge.png");

    private final boolean solid;
    private final String resourcePath;

    TileType(boolean solid, String resourcePath) {
        this.solid = solid;
        this.resourcePath = resourcePath;
    }

    public boolean isSolid() {
        return solid;
    }

    public String getResourcePath() {
        return resourcePath;
    }
}
