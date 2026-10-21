package game.entity;

import game.world.GameMap;

public class Player extends Entity {

    private static final double SPEED = 200.0;

    /** Each animation image stays visible this long (seconds). */
    private static final double ANIMATION_FRAME_TIME = 0.15;
    private static final int ANIMATION_FRAME_COUNT = 2;

    private Direction facingDirection = Direction.DOWN;

    private boolean moving;

    private double animationTimer;
    private int animationFrame;

    private double directionX;
    private double directionY;

    public Player(double x, double y, int width, int height) {
        super(x, y, width, height);
    }

    /**
     * Tells the player where to walk this frame. Each axis is -1, 0 or 1.
     * The player doesn't know about the keyboard: Game decides what the
     * input means (for example, it sends 0,0 during a conversation).
     */
    public void setMoveIntent(int dx, int dy) {
        directionX = dx;
        directionY = dy;
    }

    @Override
    public void update(double deltaTime, GameMap map) {
        moving = directionX != 0 || directionY != 0;

        updateFacingDirection();

        double scale = (directionX != 0 && directionY != 0) ? 1.0 / Math.sqrt(2) : 1.0;

        moveBy(
                directionX * scale * SPEED * deltaTime,
                directionY * scale * SPEED * deltaTime,
                map
        );

        updateAnimation(deltaTime);
    }

    /** Vertical direction has priority when moving diagonally. */
    private void updateFacingDirection() {
        if (directionY < 0) {
            facingDirection = Direction.UP;
        } else if (directionY > 0) {
            facingDirection = Direction.DOWN;
        } else if (directionX < 0) {
            facingDirection = Direction.LEFT;
        } else if (directionX > 0) {
            facingDirection = Direction.RIGHT;
        }
    }

    private void updateAnimation(double deltaTime) {
        if (!moving) {
            // Standing still: show the first frame.
            animationFrame = 0;
            animationTimer = 0;
            return;
        }

        animationTimer += deltaTime;

        if (animationTimer >= ANIMATION_FRAME_TIME) {
            animationFrame = (animationFrame + 1) % ANIMATION_FRAME_COUNT;
            animationTimer -= ANIMATION_FRAME_TIME;
        }
    }

    public Direction getFacingDirection() {
        return facingDirection;
    }

    public boolean isMoving() {
        return moving;
    }

    public int getAnimationFrame() {
        return animationFrame;
    }

    public double getSpeed() {
        return SPEED;
    }
}
