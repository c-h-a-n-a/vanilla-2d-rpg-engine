package game.render;

import game.config.GameConfig;
import game.entity.CollisionBox;
import game.entity.Entity;
import game.entity.Player;
import game.state.Game;
import game.world.Camera;
import game.world.CollisionDebugInfo;
import game.world.GameMap;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * Developer overlay. Press F3 in the game to toggle it.
 * Shows collision boxes, the tiles being checked around the player, and stats.
 */
public class DebugOverlay {

    private static final Font FONT = new Font("Monospaced", Font.PLAIN, 13);

    private static final Color PANEL = new Color(0, 0, 0, 190);
    private static final Color CHECKED = Color.YELLOW;
    private static final Color BLOCKED = Color.RED;
    private static final Color BOX_COLOR = Color.RED;

    private final Game game;

    public DebugOverlay(Game game) {
        this.game = game;
    }

    public void render(Graphics2D graphics, int fps) {
        Camera camera = game.getCamera();

        drawCollisionBoxes(graphics, camera);
        drawCheckedTiles(graphics, camera);
        drawStatsPanel(graphics, fps);
    }

    private void drawCollisionBoxes(Graphics2D graphics, Camera camera) {
        graphics.setColor(BOX_COLOR);

        for (Entity entity : game.getEntities()) {
            CollisionBox box = entity.getCollisionBox();

            graphics.drawRect(
                    camera.worldToScreenX(entity.getX() + box.offsetX()),
                    camera.worldToScreenY(entity.getY() + box.offsetY()),
                    box.width(),
                    box.height()
            );
        }
    }

    /** Highlights the tiles that the player's collision box touches right now. */
    private void drawCheckedTiles(Graphics2D graphics, Camera camera) {
        Player player = game.getPlayer();
        GameMap map = game.getCurrentMap();
        CollisionBox box = player.getCollisionBox();

        CollisionDebugInfo info = map.inspect(
                player.getX() + box.offsetX(),
                player.getY() + box.offsetY(),
                box.width(),
                box.height()
        );

        int tileSize = GameConfig.TILE_SIZE;

        graphics.setColor(CHECKED);

        for (int row = info.startRow(); row <= info.endRow(); row++) {
            for (int column = info.startColumn(); column <= info.endColumn(); column++) {
                graphics.drawRect(
                        camera.worldToScreenX(column * tileSize),
                        camera.worldToScreenY(row * tileSize),
                        tileSize,
                        tileSize
                );
            }
        }

        if (info.blocked() && info.blockingColumn() >= 0) {
            graphics.setColor(BLOCKED);
            graphics.drawRect(
                    camera.worldToScreenX(info.blockingColumn() * tileSize),
                    camera.worldToScreenY(info.blockingRow() * tileSize),
                    tileSize,
                    tileSize
            );
        }
    }

    private void drawStatsPanel(Graphics2D graphics, int fps) {
        Player player = game.getPlayer();
        int tileSize = GameConfig.TILE_SIZE;

        String[] lines = {
                "DEBUG (F3 to hide)",
                "FPS: " + fps,
                String.format("Position: %.1f, %.1f", player.getX(), player.getY()),
                String.format("Tile: [%d, %d]",
                        (int) Math.floor(player.getX() / tileSize),
                        (int) Math.floor(player.getY() / tileSize)),
                "Facing: " + player.getFacingDirection(),
                "Moving: " + player.isMoving(),
                "Animation frame: " + player.getAnimationFrame(),
                "Blocked X: " + player.isBlockedX() + "  Y: " + player.isBlockedY(),
                "Entities: " + game.getEntities().size()
        };

        int panelX = 10;
        int panelY = 10;
        int lineHeight = 18;

        graphics.setColor(PANEL);
        graphics.fillRoundRect(panelX, panelY, 280, lines.length * lineHeight + 16, 10, 10);

        graphics.setFont(FONT);
        graphics.setColor(Color.WHITE);

        int y = panelY + 22;

        for (String line : lines) {
            graphics.drawString(line, panelX + 12, y);
            y += lineHeight;
        }
    }
}
