package game.battle;

import java.util.List;

/** Decides the order in which the actions of a round are resolved. */
public class BattleTurnManager {

    /**
     * Orders the two actions of a round. The faster attacker goes first;
     * on equal speed the player goes first (deterministic tie-break).
     * Move priority will be added here in Milestone 4.
     *
     * @return the actions in the order they should be resolved
     */
    public List<BattleAction> orderActions(
            BattleAction playerAction,
            BattleAction enemyAction) {

        int playerSpeed = playerAction.attacker().getSpeed();
        int enemySpeed = enemyAction.attacker().getSpeed();

        if (enemySpeed > playerSpeed) {
            return List.of(enemyAction, playerAction);
        }

        return List.of(playerAction, enemyAction);
    }
}