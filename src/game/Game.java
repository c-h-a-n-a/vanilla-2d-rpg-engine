package game;

import java.awt.event.KeyEvent;

public class Game {
    private static final int PLAYER_SIZE = 32;
    private static final double PLAYER_SPEED = 200.0;

    private final KeyboardInput input;

    private double playerX = 100;
    private double playerY = 100;

    private double directionX;
    private double directionY;

    private double movementX;
    private double movementY;

    private double lastDeltaTime;

    public Game(KeyboardInput input) {
        this.input = input;
    }

    public void update(
            double deltaTime,
            int canvasWidth,
            int canvasHeight
    ) {
        lastDeltaTime = deltaTime;

        readMovementDirection();
        normalizeDiagonalMovement();
        movePlayer(deltaTime);
        keepPlayerInside(canvasWidth, canvasHeight);
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

    private void movePlayer(double deltaTime) {
        movementX = directionX * PLAYER_SPEED * deltaTime;
        movementY = directionY * PLAYER_SPEED * deltaTime;

        playerX += movementX;
        playerY += movementY;
    }

    private void keepPlayerInside(
            int canvasWidth,
            int canvasHeight
    ) {
        double maximumX = canvasWidth - PLAYER_SIZE;
        double maximumY = canvasHeight - PLAYER_SIZE;

        playerX = Math.max(0, Math.min(playerX, maximumX));
        playerY = Math.max(0, Math.min(playerY, maximumY));
    }

    public KeyboardInput getInput() {
        return input;
    }

    public double getPlayerX() {
        return playerX;
    }

    public double getPlayerY() {
        return playerY;
    }

    public int getPlayerSize() {
        return PLAYER_SIZE;
    }

    public double getPlayerSpeed() {
        return PLAYER_SPEED;
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
