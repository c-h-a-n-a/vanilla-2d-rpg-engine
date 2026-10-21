package game.world;

import game.tile.TileType;

/**
 * Snapshot of one collision query, used only by the debug overlay.
 */
public record CollisionDebugInfo(
        double x,
        double y,
        double width,
        double height,
        int startColumn,
        int endColumn,
        int startRow,
        int endRow,
        boolean blocked,
        TileType blockingTile,
        int blockingColumn,
        int blockingRow
) {
}
