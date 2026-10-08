package game;

import game.entity.Player;
import game.tile.TileManager;
import game.world.WorldMap;

public class Game {

    private final Player player;
    private final KeyboardInput input;
    private final WorldMap worldMap;

    public Game(
            KeyboardInput input
    ) {

        this.input = input;

        /*
         * Create the world.
         */
        TileManager tileManager =
                new TileManager();

        worldMap =
                new WorldMap(
                        tileManager
                );

        /*
         * Player position is a WORLD position.
         *
         * x = 300
         * y = 200
         */
        player = new Player(
                1000,
                400,
                64,
                64,
                input
        );
    }

    public void update(
            double deltaTime
    ) {

        /*
         * Update player movement.
         */
        player.update(
                deltaTime,
                worldMap
        );

        /*
         * Keep the player inside
         * the WORLD boundaries.
         */
        player.keepInside(
                worldMap.getWorldWidth(),
                worldMap.getWorldHeight()
        );
    }

    public Player getPlayer() {
        return player;
    }

    public KeyboardInput getInput() {
        return input;
    }

    public WorldMap getWorldMap() {
        return worldMap;
    }
}