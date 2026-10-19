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

        //loadDungeonTiles();
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

    private void loadDungeonTiles() {

        try {

            BufferedImage dungeonImage =
                    ImageIO.read(getClass().getResourceAsStream("/tiles/dungeon/dungeon_dirt.png"));

            BufferedImage dungeonWallUpImage =
                    ImageIO.read(getClass().getResourceAsStream("/tiles/dungeon/dungeon_wall_up.png"));

            BufferedImage dungeonWallDownImage =
                    ImageIO.read(getClass().getResourceAsStream("/tiles/dungeon/dungeon_wall_down.png"));

            BufferedImage dungeonWallLeftDownImage =
                    ImageIO.read(getClass().getResourceAsStream("/tiles/dungeon/dungeon_wall_left_down.png"));

            BufferedImage dungeonWallRightDownImage =
                    ImageIO.read(getClass().getResourceAsStream("/tiles/dungeon/dungeon_wall_right_down.png"));

            BufferedImage dungeonWallLeftUpImage =
                    ImageIO.read(getClass().getResourceAsStream("/tiles/dungeon/dungeon_wall_up_left.png"));

            BufferedImage dungeonWallRightUpImage =
                    ImageIO.read(getClass().getResourceAsStream("/tiles/dungeon/dungeon_wall_up_right.png"));

            BufferedImage dungeonWallLeftImage =
                    ImageIO.read(getClass().getResourceAsStream("/tiles/dungeon/dungeon_wall_left.png"));

            BufferedImage dungeonWallRightImage =
                    ImageIO.read(getClass().getResourceAsStream("/tiles/dungeon/dungeon_wall_right.png"));

            BufferedImage dungeonWallLeftEdge =
                    ImageIO.read(getClass().getResourceAsStream("/tiles/dungeon/dungeon_wall_left_edge.png"));

            BufferedImage dungeonWallRightEdge =
                    ImageIO.read(getClass().getResourceAsStream("/tiles/dungeon/dungeon_wall_right_edge.png"));


            tiles.put(
                    TileType.DUNGEON_DIRT,
                    new Tile(dungeonImage)
            );

            tiles.put(
                    TileType.DUNGEON_WALL_UP,
                    new Tile(dungeonWallUpImage)
            );

            tiles.put(
                    TileType.DUNGEON_WALL_DOWN,
                    new Tile(dungeonWallDownImage)
            );

            tiles.put(
                    TileType.DUNGEON_WALL_RIGHT,
                    new Tile(dungeonWallRightImage)
            );

            tiles.put(
                    TileType.DUNGEON_WALL_LEFT,
                    new Tile(dungeonWallLeftImage)
            );

            tiles.put(
                    TileType.DUNGEON_WALL_LEFT_UP,
                    new Tile(dungeonWallLeftUpImage)
            );

            tiles.put(
                    TileType.DUNGEON_WALL_RIGHT_UP,
                    new Tile(dungeonWallRightUpImage)
            );

            tiles.put(
                    TileType.DUNGEON_WALL_LEFT_DOWN,
                    new Tile(dungeonWallLeftDownImage)
            );

            tiles.put(
                    TileType.DUNGEON_WALL_RIGHT_DOWN,
                    new Tile(dungeonWallRightDownImage)
            );

            tiles.put(
                    TileType.DUNGEON_WALL_LEFT_EDGE,
                    new Tile(dungeonWallLeftEdge)
            );

            tiles.put(
                    TileType.DUNGEON_WALL_RIGHT_EDGE,
                    new Tile(dungeonWallRightEdge)
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
