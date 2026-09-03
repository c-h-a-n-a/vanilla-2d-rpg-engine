package game;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;

public class GameCanvas extends Canvas {
    private static final int PLAYER_SIZE = 32;
    private static final double PLAYER_SPEED = 200.0; // 200 pixel per seconds not 200 pixel per update

    private final KeyboardInput input;

    private double playerX = 100;
    private double playerY = 100;

    //fields inside update but need to be global for logging purposes
    private double directionX;
    private double directionY;

    private double movementX;
    private double movementY;

    private double lastDeltaTime;

    //logging fps
    private int currentFps;
    private int framesThisSecond;
    private long fpsTimer = System.nanoTime();


    public GameCanvas(int width,
                      int height,
                      KeyboardInput input
    ) {
        this.input = input;

        setPreferredSize(new Dimension(width, height));
        setBackground(Color.BLACK);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        /*
         * The game performs active rendering through
         * BufferStrategy instead of AWT repaint events.
         */
        setIgnoreRepaint(true);

        addKeyListener(input);
        addFocusListener(input);
    }

    public void initialize() {
        if (!isDisplayable()) {
            throw new IllegalStateException(
                    "GameCanvas must be visible before initialization."
            );
        }

        createBufferStrategy(3);
        requestFocusInWindow();
    }

    public void update(double deltaTime) {
        /*
         * Save deltaTime so we can display it
         * in the debug panel.
         */
        lastDeltaTime = deltaTime;

        /*
         * Reset the direction every update.
         */
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

        /*
         * Normalize diagonal movement.
         */
        if (directionX != 0 && directionY != 0) {
            double diagonalScale = 1.0 / Math.sqrt(2);

            directionX *= diagonalScale;
            directionY *= diagonalScale;
        }

        /*
         * Calculate how much the player moves
         * during this update.
         */
        movementX = directionX * PLAYER_SPEED * deltaTime;
        movementY = directionY * PLAYER_SPEED * deltaTime;

        /*
         * Apply the movement to the actual position.
         */
        playerX += movementX;
        playerY += movementY;

        keepPlayerInsideCanvas();
    }

    private void keepPlayerInsideCanvas() {
        double maximumX = getWidth() - PLAYER_SIZE;
        double maximumY = getHeight() - PLAYER_SIZE;

        playerX = Math.max(0, Math.min(playerX, maximumX));
        playerY = Math.max(0, Math.min(playerY, maximumY));
    }

    public void render() {

        BufferStrategy bufferStrategy = getBufferStrategy();

        if (bufferStrategy == null) {
            return;
        }

        do {
            do {
                Graphics2D graphics =
                        (Graphics2D) bufferStrategy.getDrawGraphics();

                try {
                    drawFrame(graphics);
                } finally {
                    graphics.dispose();
                }

            } while (bufferStrategy.contentsRestored());

            bufferStrategy.show();

        } while (bufferStrategy.contentsLost());

        /*
         * Helps synchronize graphics on some operating systems,
         * especially Linux.
         */
        Toolkit.getDefaultToolkit().sync();

        /*
         * A complete frame was rendered and displayed.
         */
        updateFpsCounter();
    }

    private void drawFrame(Graphics2D graphics) {
        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        drawBackground(graphics);
        drawPlayer(graphics);
        drawInstructions(graphics);
        /*
         * Draw this last so it appears above
         * the game world and player.
         */
        drawDebugPanel(graphics);
    }

    private void drawBackground(Graphics2D graphics) {
        graphics.setColor(new Color(45, 120, 65));
        graphics.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawPlayer(Graphics2D graphics) {
        graphics.setColor(new Color(50, 100, 220));

        graphics.fillRect(
                (int) Math.round(playerX),
                (int) Math.round(playerY),
                PLAYER_SIZE,
                PLAYER_SIZE
        );

        graphics.setColor(Color.WHITE);

        graphics.drawRect(
                (int) Math.round(playerX),
                (int) Math.round(playerY),
                PLAYER_SIZE,
                PLAYER_SIZE
        );
    }

    private void drawInstructions(Graphics2D graphics) {
        graphics.setColor(Color.WHITE);
        graphics.drawString(
                "Move: WASD or Arrow Keys",
                15,
                25
        );
    }

    private void drawDebugPanel(Graphics2D graphics) {
        int panelX = 10;
        int panelY = 40;
        int panelWidth = 420;
        int panelHeight = 255;

        /*
         * Draw the debug panel background.
         */
        graphics.setColor(new Color(0, 0, 0, 190));
        graphics.fillRoundRect(
                panelX,
                panelY,
                panelWidth,
                panelHeight,
                10,
                10
        );

        graphics.setColor(Color.WHITE);

        int textX = panelX + 15;
        int textY = panelY + 25;
        int lineHeight = 22;

        graphics.drawString(
                "PLAYER MOVEMENT DEBUG",
                textX,
                textY
        );

        textY += lineHeight;

        graphics.drawString(
                String.format(
                        "Position:  X = %.2f   Y = %.2f",
                        playerX,
                        playerY
                ),
                textX,
                textY
        );

        textY += lineHeight;

        graphics.drawString(
                String.format(
                        "Direction: X = %.3f   Y = %.3f",
                        directionX,
                        directionY
                ),
                textX,
                textY
        );

        textY += lineHeight;

        graphics.drawString(
                String.format(
                        "Speed: %.2f pixels/second",
                        PLAYER_SPEED
                ),
                textX,
                textY
        );

        textY += lineHeight;

        graphics.drawString(
                String.format(
                        "Delta time: %.4f seconds",
                        lastDeltaTime
                ),
                textX,
                textY
        );

        textY += lineHeight;

        graphics.drawString(
                String.format(
                        "Movement this update: X = %.3f   Y = %.3f",
                        movementX,
                        movementY
                ),
                textX,
                textY
        );

        textY += lineHeight;

        graphics.drawString(
                String.format(
                        "X formula: %.3f x %.0f x %.4f = %.3f",
                        directionX,
                        PLAYER_SPEED,
                        lastDeltaTime,
                        movementX
                ),
                textX,
                textY
        );

        textY += lineHeight;

        graphics.drawString(
                String.format(
                        "Y formula: %.3f x %.0f x %.4f = %.3f",
                        directionY,
                        PLAYER_SPEED,
                        lastDeltaTime,
                        movementY
                ),
                textX,
                textY
        );

        textY += lineHeight;

        graphics.drawString(
                getPressedKeysText(),
                textX,
                textY
        );

        textY += lineHeight;

        graphics.drawString(
                "FPS: " + currentFps,
                textX,
                textY
        );

        textY += lineHeight;

        graphics.drawString(
                "Canvas: " + getWidth() + " x " + getHeight(),
                textX,
                textY
        );


    }

    //helper method
    private String getPressedKeysText() {
        StringBuilder keys = new StringBuilder("Pressed keys: ");

        if (input.isPressed(KeyEvent.VK_W)
                || input.isPressed(KeyEvent.VK_UP)) {
            keys.append("UP ");
        }

        if (input.isPressed(KeyEvent.VK_S)
                || input.isPressed(KeyEvent.VK_DOWN)) {
            keys.append("DOWN ");
        }

        if (input.isPressed(KeyEvent.VK_A)
                || input.isPressed(KeyEvent.VK_LEFT)) {
            keys.append("LEFT ");
        }

        if (input.isPressed(KeyEvent.VK_D)
                || input.isPressed(KeyEvent.VK_RIGHT)) {
            keys.append("RIGHT ");
        }

        if (keys.toString().equals("Pressed keys: ")) {
            keys.append("NONE");
        }

        return keys.toString();
    }

    private void updateFpsCounter() {
        framesThisSecond++;

        long currentTime = System.nanoTime();
        long elapsedTime = currentTime - fpsTimer;

        /*
         * One second has passed.
         */
        if (elapsedTime >= 1_000_000_000L) {
            currentFps = framesThisSecond;
            framesThisSecond = 0;
            fpsTimer = currentTime;
        }
    }
}
