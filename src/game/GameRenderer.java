package game;

import game.entity.Direction;
import game.entity.Monster;
import game.entity.Npc;
import game.entity.Player;
import game.world.Camera;
import game.world.CollisionDebugInfo;
import game.world.DungeonMap;
import game.world.WorldMap;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class GameRenderer {

    private final Game game;
    private final Map<String, BufferedImage> portraitCache = new HashMap<>();

    private final Map<
            Direction,
            BufferedImage[]
            > playerSprites;

    private final WorldMap worldMap;
    //private final DungeonMap dungeonMap;

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
        this.worldMap = game.getWorldMap();

        //this.dungeonMap = game.getDungeonMap();

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

        drawNpcs(graphics);
        drawMonsters(graphics);

        /*
         * Render PLAYER.
         */
        drawPlayer(
                graphics
        );

        drawDialogueBox(graphics, screenWidth, screenHeight);

        /*
         * Collision visualization.
         */
        //drawDebugCollisionTiles(graphics);

        //drawPlayerCollisionBox(graphics);

        /*
         * Debug information.
         */
        //drawDebugCollisionPanel(graphics);
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
                        8,
                        14,
                        9
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


    private BufferedImage loadPortrait(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            return null;
        }

        if (portraitCache.containsKey(resourcePath)) {
            return portraitCache.get(resourcePath);
        }

        try (InputStream stream =
                     getClass().getResourceAsStream(resourcePath)) {

            if (stream == null) {
                System.err.println(
                        "Portrait not found: " + resourcePath
                );
                return null;
            }

            BufferedImage image = javax.imageio.ImageIO.read(stream);

            if (image != null) {
                portraitCache.put(resourcePath, image);
            }

            return image;

        } catch (IOException exception) {
            System.err.println(
                    "Failed to load portrait: " + resourcePath
            );
            return null;
        }
    }

    private void drawPortrait(
            Graphics2D graphics,
            Npc npc,
            int x,
            int y,
            int size
    ) {
        BufferedImage portrait = loadPortrait(npc.getPortraitPath());

        // Temporary placeholder if the portrait is not available.
        if (portrait == null) {
            graphics.setColor(new Color(65, 80, 105));
            graphics.fillRoundRect(x, y, size, size, 10, 10);

            graphics.setColor(Color.WHITE);
            graphics.setFont(new Font("Monospaced", Font.BOLD, 20));

            String initials = npc.getName().isBlank()
                    ? "?"
                    : npc.getName().substring(0, 1).toUpperCase();

            graphics.drawString(
                    initials,
                    x + size / 2 - 6,
                    y + size / 2 + 7
            );

            return;
        }

        graphics.drawImage(portrait, x, y, size, size, null);
    }



    private void drawDialogueBox(
            Graphics2D graphics,
            int screenWidth,
            int screenHeight
    ) {
        Npc npc = game.getActiveNpc();

        // No active NPC means no dialogue box.
        if (npc == null) {
            return;
        }

        int margin = 24;
        int boxHeight = 140;
        int boxX = margin;
        int boxY = screenHeight - boxHeight - margin;
        int boxWidth = screenWidth - margin * 2;

        // Dialogue background.
        graphics.setColor(new Color(15, 20, 30, 235));
        graphics.fillRoundRect(
                boxX, boxY, boxWidth, boxHeight, 16, 16
        );

        // Dialogue border.
        graphics.setColor(new Color(230, 230, 240));
        graphics.drawRoundRect(
                boxX, boxY, boxWidth, boxHeight, 16, 16
        );

        // NPC name.
//        graphics.setFont(new Font("Monospaced", Font.BOLD, 18));
//        graphics.setColor(new Color(255, 220, 120));
//        graphics.drawString(
//                npc.getName(),
//                boxX + 20,
//                boxY + 30
//        );

        // NPC dialogue.
        int portraitSize = 128;
        int portraitX = boxX + 18;
        int portraitY = boxY + 24;

        drawPortrait(
                graphics,
                npc,
                portraitX,
                portraitY,
                portraitSize
        );

        int textX = portraitX + portraitSize + 20;
        int textWidth = boxX + boxWidth - textX - 20;

        graphics.setFont(new Font("Monospaced", Font.BOLD, 18));
        graphics.setColor(new Color(255, 220, 120));
        graphics.drawString(npc.getName(), textX, boxY + 34);

        graphics.setFont(new Font("Monospaced", Font.PLAIN, 15));
        graphics.setColor(Color.WHITE);

        drawWrappedText(
                graphics,
                game.getActiveDialogueLine(),
                textX,
                boxY + 62,
                textWidth,
                21
        );

        graphics.setFont(new Font("Monospaced", Font.PLAIN, 12));
        graphics.setColor(new Color(200, 200, 200));
        graphics.drawString(
                "E - Next",
                boxX + boxWidth - 100,
                boxY + boxHeight - 14
        );

        // Input hint.
//        graphics.setFont(new Font("Monospaced", Font.PLAIN, 12));
//        graphics.setColor(new Color(200, 200, 200));
//        graphics.drawString(
//                "E - Close",
//                boxX + boxWidth - 100,
//                boxY + boxHeight - 14
//        );
    }

    private void drawWrappedText(
            Graphics2D graphics,
            String text,
            int x,
            int y,
            int maxWidth,
            int lineHeight
    ) {
        String[] words = text.split("\\s+");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            String candidate = line.isEmpty()
                    ? word
                    : line + " " + word;

            if (graphics.getFontMetrics().stringWidth(candidate)
                    > maxWidth && !line.isEmpty()) {

                graphics.drawString(line.toString(), x, y);
                y += lineHeight;

                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(candidate);
            }
        }

        if (!line.isEmpty()) {
            graphics.drawString(line.toString(), x, y);
        }
    }


    private void drawPlayerCollisionBox(
            Graphics2D graphics
    ) {

        Player player =
                game.getPlayer();

        int collisionWidth = 32;
        int collisionHeight = 20;

        int collisionOffsetX = 16;
        int collisionOffsetY = 40;

        int screenX =
                camera.worldToScreenX(
                        player.getX()
                                + collisionOffsetX
                );

        int screenY =
                camera.worldToScreenY(
                        player.getY()
                                + collisionOffsetY
                );

        graphics.setColor(
                Color.RED
        );

        graphics.drawRect(
                screenX,
                screenY,
                collisionWidth,
                collisionHeight
        );
    }


    private void drawNpcs(Graphics2D graphics) {

        for (Npc npc : game.getNpcs()) {

            int screenX =
                    camera.worldToScreenX(npc.getX());

            int screenY =
                    camera.worldToScreenY(npc.getY());

            graphics.setColor(new Color(60, 140, 240));

            graphics.fillRect(
                    screenX,
                    screenY,
                    npc.getWidth(),
                    npc.getHeight()
            );

            graphics.setColor(Color.WHITE);

            graphics.drawString(
                    "NPC",
                    screenX + 18,
                    screenY + 34
            );
        }
    }


    private void drawMonsters(Graphics2D graphics) {

        for (Monster monster : game.getMonsters()) {

            int screenX =
                    camera.worldToScreenX(monster.getX());

            int screenY =
                    camera.worldToScreenY(monster.getY());

            graphics.setColor(new Color(210, 70, 70));

            graphics.fillRect(
                    screenX,
                    screenY,
                    monster.getWidth(),
                    monster.getHeight()
            );

            graphics.setColor(Color.WHITE);

            graphics.drawString(
                    "MON",
                    screenX + 12,
                    screenY + 34
            );
        }
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

        Player player =
                game.getPlayer();

        /*
         * ---------------------------------
         * X COLLISION DEBUG
         * ---------------------------------
         *
         * This is the collision check for
         * LEFT / RIGHT movement.
         *
         * Yellow = tiles being checked.
         * Red    = tile blocking movement.
         */
        drawCollisionDebugInfo(
                graphics,
                player.getXCollisionDebugInfo(),
                Color.YELLOW,
                Color.RED
        );

        /*
         * ---------------------------------
         * Y COLLISION DEBUG
         * ---------------------------------
         *
         * This is the collision check for
         * UP / DOWN movement.
         *
         * Cyan    = tiles being checked.
         * Magenta = tile blocking movement.
         */
        drawCollisionDebugInfo(
                graphics,
                player.getYCollisionDebugInfo(),
                Color.CYAN,
                Color.MAGENTA
        );
    }

    private void drawCollisionDebugInfo(
            Graphics2D graphics,
            CollisionDebugInfo debug,
            Color checkedColor,
            Color blockedColor
    ) {

        /*
         * There is no debug information yet.
         */
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
                        checkedColor.getRed(),
                        checkedColor.getGreen(),
                        checkedColor.getBlue(),
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

                /*
                 * Transparent fill.
                 */
                graphics.fillRect(
                        screenX,
                        screenY,
                        tileSize,
                        tileSize
                );

                /*
                 * Solid outline.
                 */
                graphics.setColor(
                        checkedColor
                );

                graphics.drawRect(
                        screenX,
                        screenY,
                        tileSize,
                        tileSize
                );

                /*
                 * Restore transparent color
                 * for the next tile.
                 */
                graphics.setColor(
                        new Color(
                                checkedColor.getRed(),
                                checkedColor.getGreen(),
                                checkedColor.getBlue(),
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

            /*
             * Transparent blocked-tile fill.
             */
            graphics.setColor(
                    new Color(
                            blockedColor.getRed(),
                            blockedColor.getGreen(),
                            blockedColor.getBlue(),
                            100
                    )
            );

            graphics.fillRect(
                    screenX,
                    screenY,
                    tileSize,
                    tileSize
            );

            /*
             * Solid blocked-tile outline.
             */
            graphics.setColor(
                    blockedColor
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
        int panelHeight = 400;

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
        textY += lineHeight;

        graphics.drawString(
                String.format(
                        "Sprite: W=%d H=%d",
                        player.getWidth(),
                        player.getHeight()
                ),
                textX,
                textY
        );

        textY += lineHeight;

        graphics.drawString(
                "Collision: W=32 H=20",
                textX,
                textY
        );

        textY += lineHeight;

        graphics.drawString(
                "Collision Offset: X=16 Y=40",
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