package game.battle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Decides the order in which the actions of a round are resolved. */
public class BattleTurnManager {

    /**
     * Orders a round's actions from fastest attacker to slowest. The sort is
     * stable, so equal speeds keep the order they were supplied in: callers
     * pass player slots first (slot 0, then slot 1), then the enemy, which
     * gives a deterministic tie-break. Move priority joins this in Milestone 4.
     *
     * @return a new list in resolution order
     */
    public List<BattleAction> orderActions(List<BattleAction> actions) {
        List<BattleAction> ordered = new ArrayList<>(actions);

        ordered.sort(Comparator.comparingInt(
                (BattleAction action) -> action.attacker().getSpeed()).reversed());

        return ordered;
    }
}