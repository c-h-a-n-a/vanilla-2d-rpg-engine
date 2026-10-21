package game.render;

import game.assets.AssetManager;
import game.entity.Direction;
import game.entity.Entity;
import game.entity.Monster;
import game.entity.MonsterType;
import game.entity.Npc;
import game.entity.Player;
import game.world.Camera;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Draws the player, NPCs and monsters, sorted by their feet position so that
 * whoever is lower on the screen is drawn in front.
 */
public class EntityRenderer {

    private static final int ANIMATION_FRAMES = 2;
    private static final Color NPC_COLOR = new Color(60, 140, 240);

    private final Map<Direction, BufferedImage[]> playerSprites = new EnumMap<>(Direction.class);
    private final Map<MonsterType, Color> monsterColors = new EnumMap<>(MonsterType.class);

    /** Reused every frame to avoid allocating a new list 60 times per second. */
    private final List<Entity> drawOrder = new ArrayList<>();

    private static final Comparator<Entity> BY_FEET = Comparator.comparingDouble(Entity::getFootY);

    public EntityRenderer(Player player) {
        loadPlayerSprites(player.getWidth(), player.getHeight());

        for (MonsterType type : MonsterType.values()) {
            monsterColors.put(type, new Color(type.getRgb()));
        }
    }

    private void loadPlayerSprites(int width, int height) {
        for (Direction direction : Direction.values()) {
            BufferedImage[] frames = new BufferedImage[ANIMATION_FRAMES];

            for (int frame = 0; frame < ANIMATION_FRAMES; frame++) {
                frames[frame] = AssetManager.scaledImage(
                        "/sprites/player/spriteboy_"
                                + direction.name().toLowerCase(Locale.ROOT)
                                + (frame + 1) + ".png",
                        width,
                        height
                );
            }

            playerSprites.put(direction, frames);
        }
    }

    public void render(Graphics2D graphics, Camera camera, List<Entity> entities) {
        drawOrder.clear();
        drawOrder.addAll(entities);
        drawOrder.sort(BY_FEET);

        for (Entity entity : drawOrder) {
            int screenX = camera.worldToScreenX(entity.getX());
            int screenY = camera.worldToScreenY(entity.getY());

            if (entity instanceof Player player) {
                drawPlayer(graphics, player, screenX, screenY);
            } else if (entity instanceof Npc) {
                drawPlaceholder(graphics, entity, screenX, screenY, NPC_COLOR, "NPC", 18);
            } else if (entity instanceof Monster monster) {
                drawPlaceholder(graphics, entity, screenX, screenY,
                        monsterColors.get(monster.getType()), monster.getType().getLabel(), 12);
            }
        }
    }

    private void drawPlayer(Graphics2D graphics, Player player, int screenX, int screenY) {
        BufferedImage sprite = playerSprites.get(player.getFacingDirection())[player.getAnimationFrame()];

        graphics.drawImage(sprite, screenX, screenY, null);
    }

    /** Temporary coloured box until real sprites exist. */
    private void drawPlaceholder(
            Graphics2D graphics,
            Entity entity,
            int screenX,
            int screenY,
            Color color,
            String label,
            int labelOffsetX
    ) {
        graphics.setColor(color);
        graphics.fillRect(screenX, screenY, entity.getWidth(), entity.getHeight());

        graphics.setColor(Color.WHITE);
        graphics.drawString(label, screenX + labelOffsetX, screenY + 34);
    }
}
