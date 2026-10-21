package game.world;

import game.tile.TileType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads a text map from the resources folder.
 */
public final class MapLoader {

    private MapLoader() {
    }

    public static TileType[][] load(String resourcePath, TileLegend legend) {
        List<TileType[]> rows = new ArrayList<>();

        try (InputStream inputStream = MapLoader.class.getResourceAsStream(resourcePath)) {

            if (inputStream == null) {
                throw new IOException("Could not find map: " + resourcePath);
            }

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.isEmpty()) {
                    continue;
                }

                TileType[] row = new TileType[line.length()];

                for (int column = 0; column < line.length(); column++) {
                    row[column] = legend.lookup(
                            line.charAt(column), resourcePath, rows.size(), column);
                }

                rows.add(row);
            }

        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load map: " + resourcePath, exception);
        }

        return rows.toArray(new TileType[0][]);
    }
}
