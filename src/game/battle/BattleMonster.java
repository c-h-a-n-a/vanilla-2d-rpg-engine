package game.battle;

/**
 * A monster as it exists inside a battle: stats, current HP and four moves.
 * Contains no rendering code.
 */
public class BattleMonster {

    /** Every monster has exactly this many moves (Pokémon-style). */
    public static final int MOVE_COUNT = 4;

    private final String name;

    private final int maxHp;
    private int currentHp;

    private final int attack;
    private final int defense;
    private final int speed;

    private final BattleMove[] moves;

    /**
     * Creates a monster at full HP.
     *
     * @param name    display name
     * @param maxHp   maximum (and starting) HP, must be positive
     * @param attack  attack stat
     * @param defense defense stat
     * @param speed   speed stat, decides who acts first
     * @param moves   exactly {@link #MOVE_COUNT} non-null moves
     */
    public BattleMonster(
            String name,
            int maxHp,
            int attack,
            int defense,
            int speed,
            BattleMove[] moves) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("A monster needs a name.");
        }
        if (maxHp <= 0) {
            throw new IllegalArgumentException("Max HP must be positive.");
        }
        if (moves == null || moves.length != MOVE_COUNT) {
            throw new IllegalArgumentException(
                    "A battle monster must have exactly " + MOVE_COUNT + " moves.");
        }
        for (BattleMove move : moves) {
            if (move == null) {
                throw new IllegalArgumentException("Moves cannot be null.");
            }
        }

        this.name = name;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.moves = moves.clone();
    }

    /** Reduces HP by the given amount; HP never drops below zero. */
    public void takeDamage(int damage) {
        currentHp = Math.max(0, currentHp - Math.max(0, damage));
    }

    /** Restores HP to its maximum. */
    public void restoreFullHp() {
        currentHp = maxHp;
    }

    /** Returns true when HP has reached zero. */
    public boolean isFainted() {
        return currentHp <= 0;
    }

    /** Returns the display name. */
    public String getName() {
        return name;
    }

    /** Returns maximum HP. */
    public int getMaxHp() {
        return maxHp;
    }

    /** Returns current HP. */
    public int getCurrentHp() {
        return currentHp;
    }

    /** Returns the attack stat. */
    public int getAttack() {
        return attack;
    }

    /** Returns the defense stat. */
    public int getDefense() {
        return defense;
    }

    /** Returns the speed stat. */
    public int getSpeed() {
        return speed;
    }

    /**
     * Returns the move in the given slot.
     *
     * @param index slot from 0 to {@link #MOVE_COUNT} - 1
     */
    public BattleMove getMove(int index) {
        return moves[index];
    }

    /** Returns a copy of the move array, so callers cannot modify it. */
    public BattleMove[] getMoves() {
        return moves.clone();
    }
}