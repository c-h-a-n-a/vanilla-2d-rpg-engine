package game.world;

import game.tile.TileManager;
import game.tile.TileType;

import java.awt.*;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class WorldMap {

    private TileManager tileManager;

    private TileType[][] map;

    public WorldMap(TileManager tileManager) {

        this.tileManager = tileManager;

        loadMap("/maps/world01.txt");
    }

    private void loadMap(String filePath) {

        try {

            InputStream inputStream =
                    getClass().getResourceAsStream(filePath);

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(inputStream)
                    );

            List<TileType[]> rows = new ArrayList<>();

            String line;

            while ((line = reader.readLine()) != null) {

                TileType[] row = new TileType[line.length()];

                for (int col = 0; col < line.length(); col++) {

                    char tileChar = line.charAt(col);

                    row[col] = convertCharToTile(tileChar);
                }

                rows.add(row);
            }

            map = rows.toArray(new TileType[0][]);

            reader.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private TileType convertCharToTile(char tileChar) {

        switch (tileChar) {

            case 'G':
                return TileType.GRASS;

            case 'E':
                return TileType.EARTH;

            case 'T':
                return TileType.TREE;

            case 'R':
                return TileType.ROCK;

            default:
                return TileType.GRASS;
        }
    }

    public void render(Graphics2D g) {

        for (int row = 0; row < map.length; row++) {

            for (int col = 0; col < map[row].length; col++) {

                int x = col * TileManager.TILE_SIZE;
                int y = row * TileManager.TILE_SIZE;

                tileManager.render(
                        g,
                        map[row][col],
                        x,
                        y
                );
            }
        }
    }
}
