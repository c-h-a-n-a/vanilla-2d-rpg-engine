package game.world;

import game.tile.TileManager;
import game.tile.TileType;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class WorldMap {

    private TileManager tileManager;

    private TileType[][] groundMap;
    private TileType[][] objectMap;

    public WorldMap(TileManager tileManager) {

        this.tileManager = tileManager;

        loadMaps();
    }

    private void loadMaps() {

        groundMap = loadMap(
                "/maps/ground01.txt",
                false
        );

        objectMap = loadMap(
                "/maps/objects01.txt",
                true
        );
    }

    private TileType[][] loadMap(
            String resourcePath,
            boolean objectLayer
    ) {

        List<TileType[]> rows = new ArrayList<>();

        try (InputStream inputStream =
                     getClass().getResourceAsStream(resourcePath)) {

            if (inputStream == null) {
                throw new IOException(
                        "Could not find map: " + resourcePath
                );
            }

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(inputStream)
                    );

            String line;

            while ((line = reader.readLine()) != null) {

                // Ignore empty lines
                if (line.isEmpty()) {
                    continue;
                }

                TileType[] row =
                        new TileType[line.length()];

                for (int col = 0; col < line.length(); col++) {

                    char symbol = line.charAt(col);

                    row[col] =
                            convertCharToTile(
                                    symbol,
                                    objectLayer
                            );
                }

                rows.add(row);
            }

        } catch (IOException e) {

            e.printStackTrace();
        }

        return rows.toArray(new TileType[0][]);
    }

    private TileType convertCharToTile(
            char symbol,
            boolean objectLayer
    ) {

        if (objectLayer) {

            return switch (symbol) {

                case 'T' -> TileType.TREE;
                case 'R' -> TileType.ROCK;

                // '.' means no object
                case '.' -> null;

                default -> null;
            };

        } else {

            return switch (symbol) {

                case 'G' -> TileType.GRASS;
                case 'E' -> TileType.EARTH;

                default -> TileType.GRASS;
            };
        }
    }

    private void renderGround(Graphics2D g) {

        for (int row = 0; row < groundMap.length; row++) {

            for (int col = 0; col < groundMap[row].length; col++) {

                TileType ground =
                        groundMap[row][col];

                int x =
                        col * TileManager.TILE_SIZE;

                int y =
                        row * TileManager.TILE_SIZE;

                tileManager.render(
                        g,
                        ground,
                        x,
                        y
                );
            }
        }
    }

    private void renderObjects(Graphics2D g) {

        for (int row = 0; row < objectMap.length; row++) {

            for (int col = 0; col < objectMap[row].length; col++) {

                TileType object =
                        objectMap[row][col];

                // No object at this position
                if (object == null) {
                    continue;
                }

                int x =
                        col * TileManager.TILE_SIZE;

                int y =
                        row * TileManager.TILE_SIZE;

                tileManager.render(
                        g,
                        object,
                        x,
                        y
                );
            }
        }
    }

    public void render(Graphics2D g) {

        renderGround(g);

        renderObjects(g);
    }
}
