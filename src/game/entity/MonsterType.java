package game.entity;

/**
 * The different kinds of monster. Adding an enemy = adding a line here
 * (later: sprite path, speed, health...), not a new Java class.
 */
public enum MonsterType {

    GENERIC("MON", 0xD24646),
    PINK_SLIME("SLM", 0xEB78B4),
    WOLF("WLF", 0x8B6F47);

    private final String label;
    private final int rgb;

    MonsterType(String label, int rgb) {
        this.label = label;
        this.rgb = rgb;
    }

    /** Short text drawn on the placeholder rectangle. */
    public String getLabel() {
        return label;
    }

    public int getRgb() {
        return rgb;
    }
}
