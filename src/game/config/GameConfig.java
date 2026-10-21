package game.config;

/**
 * Central place for constants shared across the engine.
 * Change a value here instead of hunting for magic numbers.
 */
public final class GameConfig {

    public static final String WINDOW_TITLE = "My 2D RPG";

    public static final int SCREEN_WIDTH = 960;
    public static final int SCREEN_HEIGHT = 576;

    public static final int TILE_SIZE = 64;

    public static final double TARGET_UPS = 60.0;
    public static final double TARGET_FPS = 60.0;

    /** Maximum distance (world pixels) for talking to an NPC. */
    public static final double INTERACTION_RANGE = 100.0;

    private GameConfig() {
    }
}
