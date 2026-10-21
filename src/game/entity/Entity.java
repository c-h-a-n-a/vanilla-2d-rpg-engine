package game.entity;

import game.world.GameMap;

import java.util.List;

/**
 * Anything that lives in the world: player, NPCs, monsters.
 *
 * Owns its position, size and collision box, and provides {@link #moveBy}
 * so every entity moves and collides in exactly the same way.
 */
public abstract class Entity {

    private double x;
    private double y;

    private final int width;
    private final int height;

    private final CollisionBox collisionBox;

    /** Everything this entity can bump into (it ignores itself). Set by Game. */
    private List<? extends Entity> obstacles = List.of();

    private boolean blockedX;
    private boolean blockedY;

    protected Entity(double x, double y, int width, int height) {
        this(x, y, width, height, CollisionBox.FEET);
    }

    protected Entity(double x, double y, int width, int height, CollisionBox collisionBox) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.collisionBox = collisionBox;
    }

    public abstract void update(double deltaTime, GameMap map);

    public void setObstacles(List<? extends Entity> obstacles) {
        this.obstacles = obstacles;
    }

    /**
     * Moves by (dx, dy), one axis at a time, so the entity slides along
     * walls instead of sticking to them. Updates {@link #isBlockedX()}
     * and {@link #isBlockedY()}.
     */
    protected final void moveBy(double dx, double dy, GameMap map) {
        blockedX = false;
        blockedY = false;

        if (dx != 0) {
            double nextX = x + dx;

            if (isBlockedAt(nextX, y, map)) {
                blockedX = true;
            } else {
                x = nextX;
            }
        }

        if (dy != 0) {
            double nextY = y + dy;

            if (isBlockedAt(x, nextY, map)) {
                blockedY = true;
            } else {
                y = nextY;
            }
        }
    }

    private boolean isBlockedAt(double nextX, double nextY, GameMap map) {
        return map.isBlocked(
                nextX + collisionBox.offsetX(),
                nextY + collisionBox.offsetY(),
                collisionBox.width(),
                collisionBox.height())
                || EntityCollision.isBlockedByEntity(this, nextX, nextY, obstacles);
    }

    /** Keeps the whole sprite inside the map. */
    public final void keepInside(GameMap map) {
        x = Math.max(0, Math.min(x, map.getWorldWidth() - width));
        y = Math.max(0, Math.min(y, map.getWorldHeight() - height));
    }

    public final void teleportTo(double newX, double newY) {
        this.x = newX;
        this.y = newY;
    }

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

    public CollisionBox getCollisionBox() {
        return collisionBox;
    }

    /** World Y of the entity's feet. Used to draw entities in the right order. */
    public double getFootY() {
        return y + collisionBox.offsetY() + collisionBox.height();
    }

    public boolean isBlockedX() {
        return blockedX;
    }

    public boolean isBlockedY() {
        return blockedY;
    }
}
