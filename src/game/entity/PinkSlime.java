package game.entity;

import game.world.WorldMap;

public class PinkSlime extends Entity {

    public PinkSlime(
            double x,
            double y,
            int width,
            int height
    ) {
        super(
                x,
                y,
                width,
                height
        );
    }

    @Override
    public void update(
            double deltaTime,
            WorldMap worldMap
    ) {
        // PinkSlime does not move or interact for now.
    }
}
