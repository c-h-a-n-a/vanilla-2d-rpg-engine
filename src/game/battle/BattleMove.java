package game.battle;

/**
 * A single move a monster can use.
 * A move currently contains its name, power and targeting type.
 */
public class BattleMove {

    /** Determines which enemy positions a move can target. */
    public enum TargetType {
        MELEE,
        RANGED
    }

    private final String name;
    private final int power;
    private final TargetType targetType;

    /**
     * Creates a melee move.
     *
     * @param name display name
     * @param power base power
     */
    public BattleMove(String name, int power) {
        this(name, power, TargetType.MELEE);
    }

    /**
     * Creates a move with an explicit targeting type.
     *
     * @param name display name
     * @param power base power
     * @param targetType melee or ranged
     */
    public BattleMove(String name, int power, TargetType targetType) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("A move needs a name.");
        }
        if (power < 0) {
            throw new IllegalArgumentException("Move power cannot be negative.");
        }
        if (targetType == null) {
            throw new IllegalArgumentException("Move target type cannot be null.");
        }

        this.name = name;
        this.power = power;
        this.targetType = targetType;
    }

    /** Returns the move's display name. */
    public String getName() {
        return name;
    }

    /** Returns the move's base power. */
    public int getPower() {
        return power;
    }

    /** Returns the move's targeting type. */
    public TargetType getTargetType() {
        return targetType;
    }
}