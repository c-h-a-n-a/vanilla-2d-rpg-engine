package game.entity;

import game.KeyboardInput;

import java.awt.event.KeyEvent;

public class Player extends Entity {

    private static final double SPEED = 200.0;

    private final KeyboardInput input;

    private double directionX;
    private double directionY;

    private double movementX;
    private double movementY;

    private double lastDeltaTime;

    public Player(
            double x,
            double y,
            int width,
            int height,
            KeyboardInput input
    ) {
        super(x, y, width, height);

        this.input = input;
    }

    @Override
    public void update(double deltaTime) {
        lastDeltaTime = deltaTime;

        readMovementDirection();
        normalizeDiagonalMovement();
        move(deltaTime);
    }

    private void readMovementDirection() {
        directionX = 0;
        directionY = 0;

        if (input.isPressed(KeyEvent.VK_W)
                || input.isPressed(KeyEvent.VK_UP)) {
            directionY--;
        }

        if (input.isPressed(KeyEvent.VK_S)
                || input.isPressed(KeyEvent.VK_DOWN)) {
            directionY++;
        }

        if (input.isPressed(KeyEvent.VK_A)
                || input.isPressed(KeyEvent.VK_LEFT)) {
            directionX--;
        }

        if (input.isPressed(KeyEvent.VK_D)
                || input.isPressed(KeyEvent.VK_RIGHT)) {
            directionX++;
        }
    }

    private void normalizeDiagonalMovement() {
        if (directionX != 0 && directionY != 0) {
            double diagonalScale = 1.0 / Math.sqrt(2);

            directionX *= diagonalScale;
            directionY *= diagonalScale;
        }
    }

    private void move(double deltaTime) {
        movementX = directionX * SPEED * deltaTime;
        movementY = directionY * SPEED * deltaTime;

        x += movementX;
        y += movementY;
    }

    public void keepInside(
            int boundaryWidth,
            int boundaryHeight
    ) {
        double maximumX = boundaryWidth - width;
        double maximumY = boundaryHeight - height;

        x = Math.max(0, Math.min(x, maximumX));
        y = Math.max(0, Math.min(y, maximumY));
    }

    public double getSpeed() {
        return SPEED;
    }

    public double getDirectionX() {
        return directionX;
    }

    public double getDirectionY() {
        return directionY;
    }

    public double getMovementX() {
        return movementX;
    }

    public double getMovementY() {
        return movementY;
    }

    public double getLastDeltaTime() {
        return lastDeltaTime;
    }
}
