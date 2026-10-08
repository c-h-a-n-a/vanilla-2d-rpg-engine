package game.entity;

import game.world.WorldMap;

public abstract class Entity {

    protected double x;
    protected double y;

    protected int width;
    protected int height;

    protected Entity(
            double x,
            double y,
            int width,
            int height
    ) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void update(
            double deltaTime,
            WorldMap worldMap
    );

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

}
