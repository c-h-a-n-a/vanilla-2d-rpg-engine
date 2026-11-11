package game.battle;

/**
 * One monster using one move on one target during a round.
 * Both the player and the enemy AI produce these.
 *
 * @param attacker the monster performing the move
 * @param defender the monster receiving it
 * @param move     the move being used
 */
public record BattleAction(
        BattleMonster attacker,
        BattleMonster defender,
        BattleMove move) {
}