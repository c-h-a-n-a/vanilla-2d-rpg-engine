package game.world;

import game.tile.TileType;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Translates the characters of a map text file into tile types.
 *
 * A legend is the only thing that differs between the overworld and the
 * dungeon, so both can use the same TileMap class.
 */
public final class TileLegend {

    private final Map<Character, TileType> mapping;
    private final TileType fallback;
    private final String emptySymbols;
    private final Set<Character> alreadyWarned = new HashSet<>();

    /**
     * @param mapping      symbol -> tile
     * @param fallback     tile used for unknown symbols (may be null for "nothing")
     * @param emptySymbols symbols that intentionally mean "no tile" (e.g. ". ")
     */
    public TileLegend(Map<Character, TileType> mapping, TileType fallback, String emptySymbols) {
        this.mapping = Map.copyOf(mapping);
        this.fallback = fallback;
        this.emptySymbols = emptySymbols;
    }

    public TileType lookup(char symbol, String source, int row, int column) {
        TileType type = mapping.get(symbol);

        if (type != null) {
            return type;
        }

        if (emptySymbols.indexOf(symbol) >= 0) {
            return null;
        }

        // Tell the developer about typos in map files, but only once per symbol.
        if (alreadyWarned.add(symbol)) {
            System.err.printf(
                    "Warning: unknown map symbol '%c' in %s (row %d, column %d), using %s%n",
                    symbol, source, row + 1, column + 1,
                    fallback == null ? "nothing" : fallback.name()
            );
        }

        return fallback;
    }
}
