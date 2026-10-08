package game;

import game.entity.Direction;
import game.entity.Player;
import game.world.Camera;
import game.world.WorldMap;
import game.world.CollisionDebugInfo;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

public class GameRenderer {

    private final Game game;

    private final Map<
            Direction,
            BufferedImage[]
            > playerSprites;

    private final WorldMap worldMap;

    private final Camera camera;

    public GameRenderer(
            Game game
    ) {

        this.game = game;

        /*
         * Player sprites.
         */
        this.playerSprites =
                loadPlayerSprites();

        /*
         * World comes from Game.
         */
        this.worldMap =
                game.getWorldMap();

        /*
         * Camera uses the actual
         * game resolution.
         */
        this.camera =
                new Camera(
                        Main.SCREEN_WIDTH,
                        Main.SCREEN_HEIGHT
                );
    }

    public void render(
            Graphics2D graphics,
            int screenWidth,
            int screenHeight,
            int fps
    ) {

        /*
         * Pixel-art rendering.
         */
        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF
        );

        drawBackground(
                graphics,
                screenWidth,
                screenHeight
        );

        Player player =
                game.getPlayer();

        /*
         * Calculate the CENTER of the player
         * in WORLD coordinates.
         */
        double playerCenterX =
                player.getX()
                        + player.getWidth() / 2.0;

        double playerCenterY =
                player.getY()
                        + player.getHeight() / 2.0;

        /*
         * Update the camera.
         */
        camera.follow(
                playerCenterX,
                playerCenterY,
                worldMap.getWorldWidth(),
                worldMap.getWorldHeight()
        );

        /*
         * Render WORLD.
         */
        worldMap.render(
                graphics,
                camera
        );

        /*
         * Render PLAYER.
         */
        drawPlayer(
                graphics
        );

        /*
         * Collision visualization.
         */
        drawDebugCollisionTiles(
                graphics
        );

        drawPlayerCollisionBox(
                graphics
        );

        /*
         * Debug information.
         */
        drawDebugCollisionPanel(
                graphics
        );
        // drawInstructions(graphics);
        // drawDebugPanel(graphics, fps);
    }

    private BufferedImage loadImage(
            String resourcePath
    ) {

        try (
                InputStream inputStream =
                        GameRenderer.class
                                .getResourceAsStream(
                                        resourcePath
                                )
        ) {

            if (inputStream == null) {

                throw new IllegalArgumentException(
                        "Image resource not found: "
                                + resourcePath
                );
            }

            return ImageIO.read(
                    inputStream
            );

        } catch (IOException exception) {

            throw new IllegalStateException(
                    "Failed to load image: "
                            + resourcePath,
                    exception
            );
        }
    }

    private Map<
            Direction,
            BufferedImage[]
            > loadPlayerSprites() {

        Map<
                Direction,
                BufferedImage[]
                > sprites =
                new EnumMap<>(
                        Direction.class
                );

        sprites.put(
                Direction.DOWN,
                new BufferedImage[]{

                        loadImage(
                                "/sprites/player/"
                                        + "spriteboy_down1.png"
                        ),

                        loadImage(
                                "/sprites/player/"
                                        + "spriteboy_down2.png"
                        )
                }
        );

        sprites.put(
                Direction.UP,
                new BufferedImage[]{

                        loadImage(
                                "/sprites/player/"
                                        + "spriteboy_up1.png"
                        ),

                        loadImage(
                                "/sprites/player/"
                                        + "spriteboy_up2.png"
                        )
                }
        );

        sprites.put(
                Direction.LEFT,
                new BufferedImage[]{

                        loadImage(
                                "/sprites/player/"
                                        + "spriteboy_left1.png"
                        ),

                        loadImage(
                                "/sprites/player/"
                                        + "spriteboy_left2.png"
                        )
                }
        );

        sprites.put(
                Direction.RIGHT,
                new BufferedImage[]{

                        loadImage(
                                "/sprites/player/"
                                        + "spriteboy_right1.png"
                        ),

                        loadImage(
                                "/sprites/player/"
                                        + "spriteboy_right2.png"
                        )
                }
        );

        return sprites;
    }

    private void drawBackground(
            Graphics2D graphics,
            int screenWidth,
            int screenHeight
    ) {

        graphics.setColor(
                new Color(
                        45,
                        120,
                        65
                )
        );

        graphics.fillRect(
                0,
                0,
                screenWidth,
                screenHeight
        );
    }

    private void drawPlayer(
            Graphics2D graphics
    ) {

        Player player =
                game.getPlayer();

        BufferedImage[] directionFrames =
                playerSprites.get(
                        player.getFacingDirection()
                );

        BufferedImage currentSprite =
                directionFrames[
                        player.getAnimationFrame()
                        ];

        /*
         * Convert player's WORLD position
         * into SCREEN position.
         */
        int screenX =
                camera.worldToScreenX(
                        player.getX()
                );

        int screenY =
                camera.worldToScreenY(
                        player.getY()
                );

        /*
         * Keep pixel art sharp.
         */
        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );

        graphics.drawImage(
                currentSprite,
                screenX,
                screenY,
                player.getWidth(),
                player.getHeight(),
                null
        );
    }

    private void drawPlayerCollisionBox(
            Graphics2D graphics
    ) {

        Player player =
                game.getPlayer();

        int screenX =
                camera.worldToScreenX(
                        player.getX()
                );

        int screenY =
                camera.worldToScreenY(
                        player.getY()
                );

        graphics.setColor(
                Color.RED
        );

        graphics.drawRect(
                screenX,
                screenY,
                player.getWidth(),
                player.getHeight()
        );
    }

    private void drawInstructions(
            Graphics2D graphics
    ) {

        graphics.setColor(
                Color.WHITE
        );

        graphics.drawString(
                "Move: WASD or Arrow Keys",
                15,
                25
        );
    }

    private void drawDebugCollisionTiles(
            Graphics2D graphics
    ) {

        CollisionDebugInfo debug =
                worldMap.getCollisionDebugInfo();

        if (debug == null) {
            return;
        }

        int tileSize =
                worldMap.getTileSize();

        /*
         * ---------------------------------
         * DRAW TILES BEING CHECKED
         * ---------------------------------
         */

        graphics.setColor(
                new Color(
                        255,
                        255,
                        0,
                        70
                )
        );

        for (
                int row = debug.getStartRow();
                row <= debug.getEndRow();
                row++
        ) {

            for (
                    int col = debug.getStartColumn();
                    col <= debug.getEndColumn();
                    col++
            ) {

                int worldX =
                        col * tileSize;

                int worldY =
                        row * tileSize;

                int screenX =
                        camera.worldToScreenX(
                                worldX
                        );

                int screenY =
                        camera.worldToScreenY(
                                worldY
                        );

                graphics.fillRect(
                        screenX,
                        screenY,
                        tileSize,
                        tileSize
                );

                graphics.setColor(
                        Color.YELLOW
                );

                graphics.drawRect(
                        screenX,
                        screenY,
                        tileSize,
                        tileSize
                );

                graphics.setColor(
                        new Color(
                                255,
                                255,
                                0,
                                70
                        )
                );
            }
        }

        /*
         * ---------------------------------
         * HIGHLIGHT BLOCKING TILE
         * ---------------------------------
         */

        if (debug.isBlocked()) {

            int worldX =
                    debug.getBlockingColumn()
                            * tileSize;

            int worldY =
                    debug.getBlockingRow()
                            * tileSize;

            int screenX =
                    camera.worldToScreenX(
                            worldX
                    );

            int screenY =
                    camera.worldToScreenY(
                            worldY
                    );

            graphics.setColor(
                    new Color(
                            255,
                            0,
                            0,
                            100
                    )
            );

            graphics.fillRect(
                    screenX,
                    screenY,
                    tileSize,
                    tileSize
            );

            graphics.setColor(
                    Color.RED
            );

            graphics.drawRect(
                    screenX,
                    screenY,
                    tileSize,
                    tileSize
            );
        }
    }

    private void drawDebugCollisionPanel(
            Graphics2D graphics
    ) {

        Player player =
                game.getPlayer();

        CollisionDebugInfo debug =
                worldMap.getCollisionDebugInfo();

        int tileSize =
                worldMap.getTileSize();

        int panelX = 10;
        int panelY = 40;
        int panelWidth = 250;
        int panelHeight = 300;

        /*
         * ---------------------------------
         * BACKGROUND
         * ---------------------------------
         */

        graphics.setColor(
                new Color(
                        0,
                        0,
                        0,
                        210
                )
        );

        graphics.fillRoundRect(
                panelX,
                panelY,
                panelWidth,
                panelHeight,
                10,
                10
        );

        /*
         * ---------------------------------
         * TEXT
         * ---------------------------------
         */

        graphics.setColor(
                Color.WHITE
        );

        int textX =
                panelX + 15;

        int textY =
                panelY + 25;

        int lineHeight = 19;

        graphics.drawString(
                "COLLISION DEBUG",
                textX,
                textY
        );

        /*
         * PLAYER
         */

        textY += lineHeight;

        graphics.drawString(
                String.format(
                        "Player: X=%.2f Y=%.2f",
                        player.getX(),
                        player.getY()
                ),
                textX,
                textY
        );

        textY += lineHeight;

        graphics.drawString(
                String.format(
                        "Size: W=%d H=%d",
                        player.getWidth(),
                        player.getHeight()
                ),
                textX,
                textY
        );

        /*
         * BOUNDS
         */

        textY += lineHeight;

        graphics.drawString(
                String.format(
                        "Left=%.2f Right=%.2f",
                        player.getX(),
                        player.getX()
                                + player.getWidth()
                ),
                textX,
                textY
        );

        textY += lineHeight;

        graphics.drawString(
                String.format(
                        "Top=%.2f Bottom=%.2f",
                        player.getY(),
                        player.getY()
                                + player.getHeight()
                ),
                textX,
                textY
        );

        /*
         * TILE
         */

        int playerColumn =
                (int) Math.floor(
                        player.getX()
                                / tileSize
                );

        int playerRow =
                (int) Math.floor(
                        player.getY()
                                / tileSize
                );

        textY += lineHeight;

        graphics.drawString(
                String.format(
                        "Player Tile: [%d, %d]",
                        playerColumn,
                        playerRow
                ),
                textX,
                textY
        );

        /*
         * MOVEMENT
         */

        textY += lineHeight;

        graphics.drawString(
                String.format(
                        "Direction: X=%.2f Y=%.2f",
                        player.getDirectionX(),
                        player.getDirectionY()
                ),
                textX,
                textY
        );

        textY += lineHeight;

        graphics.drawString(
                String.format(
                        "Movement: X=%.2f Y=%.2f",
                        player.getMovementX(),
                        player.getMovementY()
                ),
                textX,
                textY
        );

        /*
         * NEXT POSITION
         */

        double nextX =
                player.getX()
                        + player.getMovementX();

        double nextY =
                player.getY()
                        + player.getMovementY();

        textY += lineHeight;

        graphics.drawString(
                String.format(
                        "Next Position: X=%.2f Y=%.2f",
                        nextX,
                        nextY
                ),
                textX,
                textY
        );

        /*
         * TILES CHECKED
         */

        textY += lineHeight;

        graphics.drawString(
                "Tiles checked:",
                textX,
                textY
        );

        textY += lineHeight;

        graphics.drawString(
                String.format(
                        "Columns: %d -> %d",
                        debug.getStartColumn(),
                        debug.getEndColumn()
                ),
                textX + 15,
                textY
        );

        textY += lineHeight;

        graphics.drawString(
                String.format(
                        "Rows: %d -> %d",
                        debug.getStartRow(),
                        debug.getEndRow()
                ),
                textX + 15,
                textY
        );

        /*
         * ---------------------------------
         * COLLISION RESULT
         * ---------------------------------
         */

        textY += lineHeight;

        graphics.setColor(
                player.isXCollision()
                        ? Color.RED
                        : Color.GREEN
        );

        graphics.drawString(
                "X Collision: "
                        + player.isXCollision(),
                textX,
                textY
        );

        textY += lineHeight;

        graphics.setColor(
                player.isYCollision()
                        ? Color.RED
                        : Color.GREEN
        );

        graphics.drawString(
                "Y Collision: "
                        + player.isYCollision(),
                textX,
                textY
        );
    }

    private void drawDebugPanel(
            Graphics2D graphics,
            int fps
    ) {

        Player player =
                game.getPlayer();

        int panelX = 10;
        int panelY = 40;
        int panelWidth = 420;
        int panelHeight = 180;

        graphics.setColor(
                new Color(
                        0,
                        0,
                        0,
                        190
                )
        );

        graphics.fillRoundRect(
                panelX,
                panelY,
                panelWidth,
                panelHeight,
                10,
                10
        );

        graphics.setColor(
                Color.WHITE
        );

        int textX =
                panelX + 15;

        int textY =
                panelY + 25;

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
                        "Delta time: %.4f seconds",
                        player.getLastDeltaTime()
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

        textY += lineHeight;

        graphics.drawString(
                "Facing: "
                        + player.getFacingDirection(),
                textX,
                textY
        );

        textY += lineHeight;

        graphics.drawString(
                "Moving: "
                        + player.isMoving(),
                textX,
                textY
        );

        textY += lineHeight;

        graphics.drawString(
                "Animation frame: "
                        + player.getAnimationFrame(),
                textX,
                textY
        );
    }

    private String getPressedKeysText() {

        KeyboardInput input =
                game.getInput();

        StringBuilder keys =
                new StringBuilder(
                        "Keys: "
                );

        if (
                input.isPressed(
                        java.awt.event.KeyEvent.VK_W
                )
                        ||
                        input.isPressed(
                                java.awt.event.KeyEvent.VK_UP
                        )
        ) {

            keys.append("UP ");
        }

        if (
                input.isPressed(
                        java.awt.event.KeyEvent.VK_S
                )
                        ||
                        input.isPressed(
                                java.awt.event.KeyEvent.VK_DOWN
                        )
        ) {

            keys.append("DOWN ");
        }

        if (
                input.isPressed(
                        java.awt.event.KeyEvent.VK_A
                )
                        ||
                        input.isPressed(
                                java.awt.event.KeyEvent.VK_LEFT
                        )
        ) {

            keys.append("LEFT ");
        }

        if (
                input.isPressed(
                        java.awt.event.KeyEvent.VK_D
                )
                        ||
                        input.isPressed(
                                java.awt.event.KeyEvent.VK_RIGHT
                        )
        ) {

            keys.append("RIGHT ");
        }

        if (
                keys.length()
                        == "Keys: ".length()
        ) {

            keys.append("NONE");
        }

        return keys.toString();
    }
}