package game.battle;

/** Pure damage formula, kept separate so it is easy to test and rebalance. */
public final class DamageCalculator {

    private DamageCalculator() {
    }

    /**
     * Calculates damage as move power + attacker attack - defender defense,
     * with a minimum of 1 so every hit does something.
     */
    public static int calculate(
            BattleMonster attacker,
            BattleMonster defender,
            BattleMove move) {

        return Math.max(
                1,
                move.getPower() + attacker.getAttack() - defender.getDefense());
    }
}