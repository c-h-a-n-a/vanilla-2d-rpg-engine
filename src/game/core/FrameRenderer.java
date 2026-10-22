package game.core;

import java.awt.Graphics2D;

/**
 * Anything that can draw one frame. Lets the canvas and the game loop
 * stay independent from the concrete renderer.
 */
@FunctionalInterface
public interface FrameRenderer {

    void render(Graphics2D graphics, int screenWidth, int screenHeight, int fps);
}
