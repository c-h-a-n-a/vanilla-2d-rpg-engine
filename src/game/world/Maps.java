package game.world;

import game.tile.TileManager;
import game.tile.TileType;

import java.util.Map;

/**
 * Describes the game's maps: which files to load and what each character means.
 * Adding a new map = adding one factory method here.
 */
public final class Maps {

    private Maps() {
    }

    public static TileMap overworld(TileManager tileManager) {
        TileLegend groundLegend = new TileLegend(
                Map.of(
                        'G', TileType.GRASS,
                        'E', TileType.EARTH
                ),
                TileType.GRASS,
                ""
        );

        TileLegend objectLegend = new TileLegend(
                Map.of(
                        'T', TileType.TREE,
                        'R', TileType.ROCK
                ),
                null,
                ". "
        );

        return new TileMap(
                tileManager,
                MapLoader.load("/maps/worldground01.txt", groundLegend),
                MapLoader.load("/maps/worldobjects01.txt", objectLegend),
                false
        );
    }

    public static TileMap dungeon(TileManager tileManager) {
        TileLegend legend = new TileLegend(
                Map.ofEntries(
                        Map.entry('G', TileType.DUNGEON_DIRT),
                        Map.entry('U', TileType.DUNGEON_WALL_UP),
                        Map.entry('D', TileType.DUNGEON_WALL_DOWN),
                        Map.entry('L', TileType.DUNGEON_WALL_LEFT),
                        Map.entry('R', TileType.DUNGEON_WALL_RIGHT),
                        Map.entry('E', TileType.DUNGEON_WALL_LEFT_DOWN),
                        Map.entry('F', TileType.DUNGEON_WALL_RIGHT_DOWN),
                        Map.entry('H', TileType.DUNGEON_WALL_LEFT_UP),
                        Map.entry('I', TileType.DUNGEON_WALL_RIGHT_UP),
                        Map.entry('J', TileType.DUNGEON_WALL_LEFT_EDGE),
                        Map.entry('K', TileType.DUNGEON_WALL_RIGHT_EDGE)
                ),
                TileType.DUNGEON_DIRT,
                " "
        );

        // The dungeon has one layer, and holes in it (spaces) are solid void.
        return new TileMap(
                tileManager,
                MapLoader.load("/maps/dungeon01.txt", legend),
                null,
                true
        );
    }
}
