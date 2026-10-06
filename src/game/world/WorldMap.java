package game.world;

import game.tile.TileManager;
import game.tile.TileType;

import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class WorldMap {

    private final TileManager tileManager;

    private TileType[][] groundMap;
    private TileType[][] objectMap;

    private int worldWidth;
    private int worldHeight;

    public WorldMap(
            TileManager tileManager
    ) {

        this.tileManager = tileManager;

        loadMaps();

        calculateWorldSize();
    }

    private void loadMaps() {

        groundMap = loadMap(
                "/maps/worldground01.txt",
                false
        );

        objectMap = loadMap(
                "/maps/worldobjects01.txt",
                true
        );
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

                case 'T' ->
                        TileType.TREE;

                case 'R' ->
                        TileType.ROCK;

                /*
                 * No object.
                 */
                case '.' ->
                        null;

                default ->
                        null;
            };

        } else {

            return switch (symbol) {

                case 'G' ->
                        TileType.GRASS;

                case 'E' ->
                        TileType.EARTH;

                default ->
                        TileType.GRASS;
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

        renderObjects(
                graphics,
                camera
        );
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

    public int getWorldWidth() {
        return worldWidth;
    }

    public int getWorldHeight() {
        return worldHeight;
    }
}