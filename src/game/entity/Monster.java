package game.entity;

import game.world.WorldMap;

public class Monster extends Entity {

    public Monster(
            double x,
            double y,
            int width,
            int height
    ) {
        super(x, y, width, height);
    }

    @Override
    public void update(
            double deltaTime,
            WorldMap worldMap
    ) {
        /*
         * The monster is stationary for now.
         * Patrol and chase behavior can be
         * added later.
         */
    }
}
