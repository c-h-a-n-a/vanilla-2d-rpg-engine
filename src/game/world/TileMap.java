package game.world;

import game.config.GameConfig;
import game.tile.TileManager;
import game.tile.TileType;

import java.awt.Graphics2D;
import java.util.EnumSet;
import java.util.Set;

/**
 * A map made of a ground layer and an optional object layer.
 *
 * Replaces the old WorldMap and DungeonMap, which were ~90% identical.
 * A tile blocks movement if its ground OR its object is solid. The area
 * outside the map always blocks movement.
 */
public class TileMap implements GameMap {

    private static final int NONE = -1;
    private static final int OUT_OF_BOUNDS = -2;

    private final TileManager tileManager;

    private final TileType[][] ground;
    private final TileType[][] objects;

    /** If true, a missing ground tile (a hole in the map) cannot be walked on. */
    private final boolean voidIsSolid;

    private final int rows;
    private final int columns;

    public TileMap(
            TileManager tileManager,
            TileType[][] ground,
            TileType[][] objects,
            boolean voidIsSolid
    ) {
        this.tileManager = tileManager;
        this.ground = ground;
        this.objects = objects == null ? new TileType[0][] : objects;
        this.voidIsSolid = voidIsSolid;

        this.rows = ground.length;

        int widest = 0;
        for (TileType[] row : ground) {
            widest = Math.max(widest, row.length);
        }
        this.columns = widest;

        tileManager.preload(collectUsedTypes());
    }

    private Set<TileType> collectUsedTypes() {
        Set<TileType> used = EnumSet.noneOf(TileType.class);

        for (TileType[][] layer : new TileType[][][]{ground, objects}) {
            for (TileType[] row : layer) {
                for (TileType type : row) {
                    if (type != null) {
                        used.add(type);
                    }
                }
            }
        }

        return used;
    }

    // ------------------------------------------------------------------
    // Rendering
    // ------------------------------------------------------------------

    @Override
    public void render(Graphics2D graphics, Camera camera) {
        int tileSize = GameConfig.TILE_SIZE;

        int startColumn = Math.max(0, (int) Math.floor(camera.getX() / tileSize));
        int endColumn = Math.min(columns - 1,
                (int) Math.floor((camera.getX() + camera.getScreenWidth()) / tileSize));

        int startRow = Math.max(0, (int) Math.floor(camera.getY() / tileSize));
        int endRow = Math.min(rows - 1,
                (int) Math.floor((camera.getY() + camera.getScreenHeight()) / tileSize));

        renderLayer(graphics, camera, ground, startRow, endRow, startColumn, endColumn);
        renderLayer(graphics, camera, objects, startRow, endRow, startColumn, endColumn);
    }

    private void renderLayer(
            Graphics2D graphics,
            Camera camera,
            TileType[][] layer,
            int startRow,
            int endRow,
            int startColumn,
            int endColumn
    ) {
        int tileSize = GameConfig.TILE_SIZE;

        for (int row = startRow; row <= endRow && row < layer.length; row++) {

            TileType[] line = layer[row];

            for (int column = startColumn; column <= endColumn && column < line.length; column++) {

                TileType type = line[column];

                if (type == null) {
                    continue;
                }

                tileManager.render(
                        graphics,
                        type,
                        camera.worldToScreenX(column * tileSize),
                        camera.worldToScreenY(row * tileSize)
                );
            }
        }
    }

    // ------------------------------------------------------------------
    // Collision
    // ------------------------------------------------------------------

    @Override
    public boolean isBlocked(double x, double y, double width, double height) {
        return findBlockingTile(x, y, width, height) != NONE;
    }

    @Override
    public CollisionDebugInfo inspect(double x, double y, double width, double height) {
        int tileSize = GameConfig.TILE_SIZE;

        int found = findBlockingTile(x, y, width, height);

        int blockingColumn = -1;
        int blockingRow = -1;
        TileType blockingTile = null;

        if (found >= 0) {
            blockingRow = found / columns;
            blockingColumn = found % columns;
            blockingTile = solidTypeAt(blockingRow, blockingColumn);
        }

        return new CollisionDebugInfo(
                x, y, width, height,
                Math.max(0, (int) Math.floor(x / tileSize)),
                Math.min(columns - 1, (int) Math.floor((x + width - 0.001) / tileSize)),
                Math.max(0, (int) Math.floor(y / tileSize)),
                Math.min(rows - 1, (int) Math.floor((y + height - 0.001) / tileSize)),
                found != NONE,
                blockingTile,
                blockingColumn,
                blockingRow
        );
    }

    /**
     * Returns row * columns + column of the first blocking tile,
     * OUT_OF_BOUNDS if the rectangle leaves the map, or NONE.
     * Does not allocate, so it is safe to call many times per frame.
     */
    private int findBlockingTile(double x, double y, double width, double height) {
        if (width <= 0 || height <= 0) {
            return NONE;
        }

        int tileSize = GameConfig.TILE_SIZE;

        // The -0.001 stops a rectangle ending exactly on a tile edge
        // from also checking the next tile.
        int startColumn = (int) Math.floor(x / tileSize);
        int endColumn = (int) Math.floor((x + width - 0.001) / tileSize);
        int startRow = (int) Math.floor(y / tileSize);
        int endRow = (int) Math.floor((y + height - 0.001) / tileSize);

        for (int row = startRow; row <= endRow; row++) {
            for (int column = startColumn; column <= endColumn; column++) {

                if (row < 0 || row >= rows || column < 0 || column >= columns) {
                    return OUT_OF_BOUNDS;
                }

                if (isSolid(row, column)) {
                    return row * columns + column;
                }
            }
        }

        return NONE;
    }

    private boolean isSolid(int row, int column) {
        TileType groundTile = tileAt(ground, row, column);

        if (groundTile == null) {
            if (voidIsSolid) {
                return true;
            }
        } else if (groundTile.isSolid()) {
            return true;
        }

        TileType objectTile = tileAt(objects, row, column);

        return objectTile != null && objectTile.isSolid();
    }

    private TileType solidTypeAt(int row, int column) {
        TileType groundTile = tileAt(ground, row, column);

        if (groundTile != null && groundTile.isSolid()) {
            return groundTile;
        }

        TileType objectTile = tileAt(objects, row, column);

        return objectTile != null && objectTile.isSolid() ? objectTile : null;
    }

    /** Safe lookup: rows may be shorter than the widest row. */
    private static TileType tileAt(TileType[][] layer, int row, int column) {
        if (row < 0 || row >= layer.length) {
            return null;
        }

        TileType[] line = layer[row];

        if (column < 0 || column >= line.length) {
            return null;
        }

        return line[column];
    }

    // ------------------------------------------------------------------

    @Override
    public int getWorldWidth() {
        return columns * GameConfig.TILE_SIZE;
    }

    @Override
    public int getWorldHeight() {
        return rows * GameConfig.TILE_SIZE;
    }
}
