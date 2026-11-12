package game.battle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Chooses enemy moves and player targets.
 * Smarter behavior and intention display will be added in a later milestone.
 */
public class EnemyAI {

    private final Random random;

    /**
     * Creates an AI with a normal random source.
     */
    public EnemyAI() {
        this(new Random());
    }

    /**
     * Creates an AI with a supplied random source.
     *
     * @param random random number generator
     */
    public EnemyAI(Random random) {
        if (random == null) {
            throw new IllegalArgumentException("Random cannot be null.");
        }

        this.random = random;
    }

    /**
     * Picks which move an enemy will use.
     *
     * @param enemy enemy choosing a move
     * @return move index from 0 to 3
     */
    public int chooseMoveIndex(BattleMonster enemy) {
        if (enemy == null) {
            throw new IllegalArgumentException("Enemy cannot be null.");
        }

        return random.nextInt(BattleMonster.MOVE_COUNT);
    }

    /**
     * Picks a living active player monster to attack.
     *
     * @param party player's party
     * @return active slot index, or -1 if no target exists
     */
    public int chooseTargetSlot(BattleParty party) {
        if (party == null) {
            throw new IllegalArgumentException("Party cannot be null.");
        }

        List<Integer> candidates = new ArrayList<>();

        for (int slot = 0; slot < BattleParty.ACTIVE_SLOTS; slot++) {
            if (party.isSlotAlive(slot)) {
                candidates.add(slot);
            }
        }

        if (candidates.isEmpty()) {
            return -1;
        }

        return candidates.get(random.nextInt(candidates.size()));
    }
}