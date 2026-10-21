package game.entity;

import game.world.GameMap;

public class Monster extends Entity {

    private static final int SIZE = 64;

    private final MonsterType type;

    public Monster(MonsterType type, double x, double y) {
        super(x, y, SIZE, SIZE);
        this.type = type;
    }

    public MonsterType getType() {
        return type;
    }

    @Override
    public void update(double deltaTime, GameMap map) {
        // Stationary for now. Patrol and chase behaviour can be added later.
    }
}
