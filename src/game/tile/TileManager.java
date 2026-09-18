package game.tile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.EnumMap;

public class TileManager {

    public static final int TILE_SIZE = 64;

    private EnumMap<TileType, Tile> tiles;

    public TileManager() {

        tiles = new EnumMap<>(TileType.class);

        loadTiles();
    }

    private void loadTiles() {

        try {

            BufferedImage grassImage =
                    ImageIO.read(getClass().getResourceAsStream("/tiles/grass.png"));

            BufferedImage earthImage =
                    ImageIO.read(getClass().getResourceAsStream("/tiles/earth.png"));

            BufferedImage treeImage =
                    ImageIO.read(getClass().getResourceAsStream("/tiles/tree.png"));

            BufferedImage rockImage =
                    ImageIO.read(getClass().getResourceAsStream("/tiles/rock.png"));


            tiles.put(
                    TileType.GRASS,
                    new Tile(grassImage)
            );

            tiles.put(
                    TileType.EARTH,
                    new Tile(earthImage)
            );

            tiles.put(
                    TileType.TREE,
                    new Tile(treeImage)
            );

            tiles.put(
                    TileType.ROCK,
                    new Tile(rockImage)
            );

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public void render(Graphics2D g, TileType type, int x, int y) {

        Tile tile = tiles.get(type);

        if (tile == null) {
            return;
        }

        g.drawImage(
                tile.getImage(),
                x,
                y,
                TILE_SIZE,
                TILE_SIZE,
                null
        );
    }
}
