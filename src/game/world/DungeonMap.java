package game.world;

import game.tile.TileManager;
import game.tile.TileType;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DungeonMap {

    private final TileManager tileManager;

    private TileType[][] groundMap;
    private TileType[][] objectMap;

    private int worldWidth;
    private int worldHeight;
    private CollisionDebugInfo collisionDebugInfo;

    public DungeonMap(
            TileManager tileManager
    ) {
        this.tileManager = tileManager;

        loadMaps();
        calculateWorldSize();

    }

    private void loadMaps() {

        groundMap = loadMap(
                "/maps/dungeon01.txt",
                false
        );

//        objectMap = loadMap(
//                "/maps/worldobjects01.txt",
//                true
//        );
    }

    private TileType[][] loadMap(
            String resourcePath,
            boolean objectLayer
    ) {

        List<TileType[]> rows =
                new ArrayList<>();

        try (
                InputStream inputStream =
                        getClass().getResourceAsStream(
                                resourcePath
                        )
        ) {

            if (inputStream == null) {

                throw new IOException(
                        "Could not find map: "
                                + resourcePath
                );
            }

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    inputStream
                            )
                    );

            String line;

            while ((line = reader.readLine()) != null) {

                /*
                 * Ignore empty lines.
                 */
                if (line.isEmpty()) {
                    continue;
                }

                TileType[] row =
                        new TileType[line.length()];

                for (
                        int col = 0;
                        col < line.length();
                        col++
                ) {

                    char symbol =
                            line.charAt(col);

                    row[col] =
                            convertCharToTile(
                                    symbol,
                                    objectLayer
                            );
                }

                rows.add(row);
            }

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Failed to load map: "
                            + resourcePath,
                    e
            );
        }

        return rows.toArray(
                new TileType[0][]
        );
    }

    private TileType convertCharToTile(
            char symbol,
            boolean objectLayer
    ) {

        if (objectLayer) {

            return switch (symbol) {

                case 'T' -> TileType.TREE;

                case 'R' -> TileType.ROCK;

                /*
                 * No object.
                 */
                case '.' -> null;

                default -> null;
            };

        } else {

            return switch (symbol) {

                case 'G' -> TileType.DUNGEON_DIRT;

                case 'U' -> TileType.DUNGEON_WALL_UP;

                case 'L' -> TileType.DUNGEON_WALL_LEFT;

                case 'R' -> TileType.DUNGEON_WALL_RIGHT;

                case 'D' -> TileType.DUNGEON_WALL_DOWN;

                case 'E' -> TileType.DUNGEON_WALL_LEFT_DOWN;

                case 'F' -> TileType.DUNGEON_WALL_RIGHT_DOWN;

                case 'H' -> TileType.DUNGEON_WALL_LEFT_UP;

                case 'I' -> TileType.DUNGEON_WALL_RIGHT_UP;

                case 'J' -> TileType.DUNGEON_WALL_LEFT_EDGE;

                case 'K' -> TileType.DUNGEON_WALL_RIGHT_EDGE;

                case ' ' -> null;

                default -> TileType.DUNGEON_DIRT;
            };
        }
    }

    /**
     * Calculates the total WORLD size
     * using the ground map.
     */
    private void calculateWorldSize() {

        int columnCount = 0;

        for (TileType[] row : groundMap) {

            columnCount =
                    Math.max(
                            columnCount,
                            row.length
                    );
        }

        worldWidth =
                columnCount
                        * TileManager.TILE_SIZE;

        worldHeight =
                groundMap.length
                        * TileManager.TILE_SIZE;
    }

    /**
     * Renders the visible portion
     * of the world.
     */
    public void render(
            Graphics2D graphics,
            Camera camera
    ) {

        renderGround(
                graphics,
                camera
        );

//        renderObjects(
//                graphics,
//                camera
//        );
    }

    private void renderGround(
            Graphics2D graphics,
            Camera camera
    ) {

        int tileSize =
                TileManager.TILE_SIZE;

        /*
         * Determine the first visible column.
         */
        int startColumn =
                Math.max(
                        0,
                        (int)
                                (camera.getX()
                                        / tileSize)
                );

        /*
         * Determine the last visible column.
         */
        int endColumn =
                Math.min(
                        getColumnCount(groundMap) - 1,

                        (int)
                                (
                                        (
                                                camera.getX()
                                                        + camera.getScreenWidth()
                                        )
                                                / tileSize
                                )
                                + 1
                );

        /*
         * Determine the first visible row.
         */
        int startRow =
                Math.max(
                        0,
                        (int)
                                (camera.getY()
                                        / tileSize)
                );

        /*
         * Determine the last visible row.
         */
        int endRow =
                Math.min(
                        groundMap.length - 1,

                        (int)
                                (
                                        (
                                                camera.getY()
                                                        + camera.getScreenHeight()
                                        )
                                                / tileSize
                                )
                                + 1
                );

        for (
                int row = startRow;
                row <= endRow;
                row++
        ) {

            for (
                    int col = startColumn;
                    col <= endColumn;
                    col++
            ) {

                /*
                 * Protect against rows
                 * with different lengths.
                 */
                if (
                        col >=
                                groundMap[row].length
                ) {
                    continue;
                }

                TileType ground =
                        groundMap[row][col];

                int worldX =
                        col * tileSize;

                int worldY =
                        row * tileSize;

                /*
                 * Convert WORLD → SCREEN.
                 */
                int screenX =
                        camera.worldToScreenX(
                                worldX
                        );

                int screenY =
                        camera.worldToScreenY(
                                worldY
                        );

                tileManager.render(
                        graphics,
                        ground,
                        screenX,
                        screenY
                );
            }
        }
    }

    private void renderObjects(
            Graphics2D graphics,
            Camera camera
    ) {

        int tileSize =
                TileManager.TILE_SIZE;

        int startColumn =
                Math.max(
                        0,
                        (int)
                                (camera.getX()
                                        / tileSize)
                );

        int endColumn =
                Math.min(
                        getColumnCount(objectMap) - 1,

                        (int)
                                (
                                        (
                                                camera.getX()
                                                        + camera.getScreenWidth()
                                        )
                                                / tileSize
                                )
                                + 1
                );

        int startRow =
                Math.max(
                        0,
                        (int)
                                (camera.getY()
                                        / tileSize)
                );

        int endRow =
                Math.min(
                        objectMap.length - 1,

                        (int)
                                (
                                        (
                                                camera.getY()
                                                        + camera.getScreenHeight()
                                        )
                                                / tileSize
                                )
                                + 1
                );

        for (
                int row = startRow;
                row <= endRow;
                row++
        ) {

            for (
                    int col = startColumn;
                    col <= endColumn;
                    col++
            ) {

                if (
                        col >=
                                objectMap[row].length
                ) {
                    continue;
                }

                TileType object =
                        objectMap[row][col];

                /*
                 * No tree/rock/etc.
                 */
                if (object == null) {
                    continue;
                }

                int worldX =
                        col * tileSize;

                int worldY =
                        row * tileSize;

                /*
                 * Convert WORLD → SCREEN.
                 */
                int screenX =
                        camera.worldToScreenX(
                                worldX
                        );

                int screenY =
                        camera.worldToScreenY(
                                worldY
                        );

                tileManager.render(
                        graphics,
                        object,
                        screenX,
                        screenY
                );
            }
        }
    }

    private int getColumnCount(
            TileType[][] map
    ) {

        int columns = 0;

        for (TileType[] row : map) {

            columns =
                    Math.max(
                            columns,
                            row.length
                    );
        }

        return columns;
    }

    /**
     * Checks whether a WORLD rectangle
     * overlaps a solid object tile.
     */

    /**
     * Checks whether a WORLD rectangle overlaps
     * a solid dungeon ground tile, such as a wall.
     *
     * The dungeon uses one map layer for both
     * walkable floors and solid walls.
     */
    public boolean isBlocked(
            double x,
            double y,
            double width,
            double height
    ) {

        int tileSize = TileManager.TILE_SIZE;

        /*
         * Protect against an unloaded or empty map.
         */
        if (
                groundMap == null ||
                        groundMap.length == 0 ||
                        width <= 0 ||
                        height <= 0
        ) {
            return false;
        }

        /*
         * Determine the first column that
         * the player's rectangle can overlap.
         */
        int startColumn =
                Math.max(
                        0,
                        (int) Math.floor(
                                x / tileSize
                        )
                );

        /*
         * Subtract a tiny amount so a rectangle
         * ending exactly at a tile boundary does
         * not check the next tile unnecessarily.
         */
        int endColumn =
                Math.min(
                        getColumnCount(groundMap) - 1,
                        (int) Math.floor(
                                (x + width - 0.001)
                                        / tileSize
                        )
                );

        /*
         * Determine the first and last rows.
         */
        int startRow =
                Math.max(
                        0,
                        (int) Math.floor(
                                y / tileSize
                        )
                );

        int endRow =
                Math.min(
                        groundMap.length - 1,
                        (int) Math.floor(
                                (y + height - 0.001)
                                        / tileSize
                        )
                );

        /*
         * Assume no collision until a solid
         * ground tile is found.
         */
        collisionDebugInfo =
                new CollisionDebugInfo(
                        x,
                        y,
                        width,
                        height,
                        startColumn,
                        endColumn,
                        startRow,
                        endRow,
                        false,
                        null,
                        -1,
                        -1
                );

        /*
         * Inspect only the rows and columns
         * that the player's rectangle can overlap.
         */
        for (
                int row = startRow;
                row <= endRow;
                row++
        ) {

            /*
             * Prevent access beyond the map.
             */
            if (
                    row < 0 ||
                            row >= groundMap.length
            ) {
                continue;
            }

            /*
             * This row may be shorter than other
             * rows in an irregular dungeon map.
             */
            if (groundMap[row] == null) {
                continue;
            }

            for (
                    int col = startColumn;
                    col <= endColumn;
                    col++
            ) {

                /*
                 * Do not access columns beyond
                 * this particular row's length.
                 */
                if (
                        col < 0 ||
                                col >= groundMap[row].length
                ) {
                    continue;
                }

                /*
                 * Read the dungeon ground tile,
                 * not the unused object map.
                 */
                TileType tile =
                        groundMap[row][col];

                /*
                 * Ignore empty tiles, if any.
                 */
                if (tile == null) {
                    continue;
                }

                /*
                 * Ignore walkable tiles.
                 */
                if (!tile.isSolid()) {
                    continue;
                }

                /*
                 * Calculate this tile's
                 * position in WORLD coordinates.
                 */
                double tileX = col * tileSize;
                double tileY = row * tileSize;

                /*
                 * AABB collision detection:
                 * Does the player's rectangle
                 * overlap this solid tile?
                 */
                boolean overlaps =
                        x < tileX + tileSize
                                && x + width > tileX
                                && y < tileY + tileSize
                                && y + height > tileY;

                if (overlaps) {

                    /*
                     * Store collision information
                     * for your debug panel.
                     */
                    collisionDebugInfo =
                            new CollisionDebugInfo(
                                    x,
                                    y,
                                    width,
                                    height,
                                    startColumn,
                                    endColumn,
                                    startRow,
                                    endRow,
                                    true,
                                    tile,
                                    col,
                                    row
                            );

                    return true;
                }
            }
        }

        /*
         * No solid tile overlaps the rectangle.
         */
        return false;
    }

    public CollisionDebugInfo getCollisionDebugInfo() {
        return collisionDebugInfo;
    }

    public int getWorldWidth() {
        return worldWidth;
    }

    public int getWorldHeight() {
        return worldHeight;
    }

    public int getTileSize() {
        return TileManager.TILE_SIZE;
    }
}
