package game;

import game.entity.Player;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class GameRenderer {

    private final Game game;

    private final BufferedImage playerSprite;

    public  GameRenderer(Game game) {
        this.game = game;
        //player sprite
        playerSprite = loadImage("/sprites/spriteboy.png");
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
        //drawInstructions(graphics);
        //drawDebugPanel(graphics, fps);
    }

    // Load image from resources
    private BufferedImage loadImage(String resourcePath) {
        try (InputStream inputStream =
                     GameRenderer.class.getResourceAsStream(resourcePath)) {

            if (inputStream == null) {
                throw new IllegalArgumentException(
                        "Image resource not found: " + resourcePath
                );
            }

            return ImageIO.read(inputStream);

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Failed to load image: " + resourcePath,
                    exception
            );
        }
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
        Player player = game.getPlayer();
        int screenX = (int) Math.round(player.getX());
        int screenY = (int) Math.round(player.getY());

//        graphics.setColor(new Color(50, 100, 220));
//        graphics.fillRect(
//                screenX,
//                screenY,
//                player.getWidth(),
//                player.getHeight()
//        );
//
//        graphics.setColor(Color.WHITE);
//        graphics.drawRect(
//                screenX,
//                screenY,
//                player.getWidth(),
//                player.getHeight()
//        );
        graphics.drawImage(
                playerSprite,
                screenX,
                screenY,
                player.getWidth(),
                player.getHeight(),
                null
        );

        graphics.setColor(Color.YELLOW);// for debug mode
        graphics.drawRect(
                screenX,
                screenY,
                player.getWidth(),
                player.getHeight()
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
        Player player = game.getPlayer();
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
                        player.getX(),
                        player.getY()
                ),
                textX,
                textY
        );

        textY += lineHeight;

        graphics.drawString(
                String.format(
                        "Direction: X = %.3f   Y = %.3f",
                        player.getDirectionX(),
                        player.getDirectionY()
                ),
                textX,
                textY
        );

        textY += lineHeight;

        graphics.drawString(
                String.format(
                        "Speed: %.2f pixels/second",
                        player.getSpeed()
                ),
                textX,
                textY
        );

        textY += lineHeight;

        graphics.drawString(
                String.format(
                        "Delta time: %.4f seconds",
                        player.getLastDeltaTime()
                ),
                textX,
                textY
        );

        textY += lineHeight;

        graphics.drawString(
                String.format(
                        "Movement: X = %.3f   Y = %.3f",
                        player.getMovementX(),
                        player.getMovementY()
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
