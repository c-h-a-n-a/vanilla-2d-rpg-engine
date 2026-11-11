package game.battle;

/**
 * A single move a monster can use. Currently just a name and a power value;
 * later milestones will add type, target, priority, energy cost, etc.
 */
public class BattleMove {

    private final String name;
    private final int power;

    /**
     * Creates a move.
     *
     * @param name  display name, must not be blank
     * @param power base power, must not be negative
     */
    public BattleMove(String name, int power) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("A move needs a name.");
        }
        if (power < 0) {
            throw new IllegalArgumentException("Move power cannot be negative.");
        }

        this.name = name;
        this.power = power;
    }

    /** Returns the move's display name. */
    public String getName() {
        return name;
    }

    /** Returns the move's base power. */
    public int getPower() {
        return power;
    }
}