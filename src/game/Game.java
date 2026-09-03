package game;

import game.entity.Player;

public class Game {

    private final Player player;
    private final KeyboardInput input;

    public Game(KeyboardInput input) {
        this.input = input;
        player = new Player(
                100,
                100,
                64,
                64,
                input
        );
    }

    public void update(
            double deltaTime,
            int canvasWidth,
            int canvasHeight
    ) {
        player.update(deltaTime);
        //player.keepInside(worldWidth, worldHeight);
        player.keepInside(canvasWidth, canvasHeight);
    }

    public Player getPlayer() {
        return player;
    }

    public KeyboardInput getInput() {
        return input;
    }

}
