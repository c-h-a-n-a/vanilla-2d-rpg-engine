package game;

import java.awt.*;
import java.awt.event.KeyEvent;

public class GameRenderer {

    private final Game game;

    public  GameRenderer(Game game) {
        this.game = game;
    }

    public void render(
            Graphics2D graphics,
            int screenWidth,
            int screenHeight,
            int fps
    ) {
        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        drawBackground(graphics, screenWidth, screenHeight);
        drawPlayer(graphics);
        drawInstructions(graphics);
        //drawDebugPanel(graphics, fps);
    }

    private void drawBackground(
            Graphics2D graphics,
            int screenWidth,
            int screenHeight
    ) {
        graphics.setColor(new Color(45, 120, 65));
        graphics.fillRect(0, 0, screenWidth, screenHeight);
    }

    private void drawPlayer(Graphics2D graphics) {
        int playerX = (int) Math.round(game.getPlayerX());
        int playerY = (int) Math.round(game.getPlayerY());
        int playerSize = game.getPlayerSize();

        graphics.setColor(new Color(50, 100, 220));
        graphics.fillRect(
                playerX,
                playerY,
                playerSize,
                playerSize
        );

        graphics.setColor(Color.WHITE);
        graphics.drawRect(
                playerX,
                playerY,
                playerSize,
                playerSize
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

    private void drawDebugPanel(
            Graphics2D graphics,
            int fps
    ) {
        int panelX = 10;
        int panelY = 40;
        int panelWidth = 420;
        int panelHeight = 255;

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
                        "Position: X = %.2f   Y = %.2f",
                        game.getPlayerX(),
                        game.getPlayerY()
                ),
                textX,
                textY
        );

        textY += lineHeight;

        graphics.drawString(
                String.format(
                        "Direction: X = %.3f   Y = %.3f",
                        game.getDirectionX(),
                        game.getDirectionY()
                ),
                textX,
                textY
        );

        textY += lineHeight;

        graphics.drawString(
                String.format(
                        "Speed: %.2f pixels/second",
                        game.getPlayerSpeed()
                ),
                textX,
                textY
        );

        textY += lineHeight;

        graphics.drawString(
                String.format(
                        "Delta time: %.4f seconds",
                        game.getLastDeltaTime()
                ),
                textX,
                textY
        );

        textY += lineHeight;

        graphics.drawString(
                String.format(
                        "Movement: X = %.3f   Y = %.3f",
                        game.getMovementX(),
                        game.getMovementY()
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
                "FPS: " + fps,
                textX,
                textY
        );
    }

    private String getPressedKeysText() {
        KeyboardInput input = game.getInput();
        StringBuilder keys = new StringBuilder("Keys: ");

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

        if (keys.length() == "Keys: ".length()) {
            keys.append("NONE");
        }

        return keys.toString();
    }
}
