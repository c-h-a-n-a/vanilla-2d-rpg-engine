package game.render;

import game.core.FrameRenderer;
import game.state.Game;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Draws one frame by running each specialised renderer in order.
 * It only READS the game state; it never changes it.
 */
public class GameRenderer implements FrameRenderer {

    private static final Color BACKGROUND = new Color(8, 14, 9);

    private final Game game;
    private final EntityRenderer entityRenderer;
    private final DialogueRenderer dialogueRenderer = new DialogueRenderer();
    private final DebugOverlay debugOverlay;

    public GameRenderer(Game game) {
        this.game = game;
        this.entityRenderer = new EntityRenderer(game.getPlayer());
        this.debugOverlay = new DebugOverlay(game);
    }

    @Override
    public void render(Graphics2D graphics, int screenWidth, int screenHeight, int fps) {
        // Pixel-art settings, applied once for the whole frame.
        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        graphics.setColor(BACKGROUND);
        graphics.fillRect(0, 0, screenWidth, screenHeight);

        game.getCurrentMap().render(graphics, game.getCamera());

        entityRenderer.render(graphics, game.getCamera(), game.getEntities());

        dialogueRenderer.render(graphics, game.getDialogue(), screenWidth, screenHeight);

        if (game.isDebugEnabled()) {
            debugOverlay.render(graphics, fps);
        }
    }
}
