package game.world;

import java.awt.Graphics2D;

/**
 * What the rest of the game needs to know about a map. The overworld and the
 * dungeon (and any future map) all implement this, so entities and the
 * renderer don't care which one is active.
 */
public interface GameMap {

    /** True if the WORLD rectangle overlaps anything solid (or leaves the map). */
    boolean isBlocked(double x, double y, double width, double height);

    /** Detailed, allocation-heavy collision report. Intended for the debug overlay only. */
    CollisionDebugInfo inspect(double x, double y, double width, double height);

    void render(Graphics2D graphics, Camera camera);

    int getWorldWidth();

    int getWorldHeight();
}
