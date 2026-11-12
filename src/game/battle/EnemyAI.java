package game.battle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Chooses the enemy's move and target. Random for now; smarter AI comes in Milestone 4. */
public class EnemyAI {

    private final Random random;

    /** Creates an AI with a normal random source. */
    public EnemyAI() {
        this(new Random());
    }

    /** Creates an AI with a supplied random source (use a seeded one in tests). */
    public EnemyAI(Random random) {
        this.random = random;
    }

    /**
     * Picks which of the enemy's moves to use this round.
     *
     * @return a move slot from 0 to {@link BattleMonster#MOVE_COUNT} - 1
     */
    public int chooseMoveIndex(BattleMonster enemy) {
        return random.nextInt(BattleMonster.MOVE_COUNT);
    }

    /**
     * Picks which living active player monster to attack.
     *
     * @return the chosen party slot, or -1 if no active monster can be targeted
     */
    public int chooseTargetSlot(BattleParty party) {
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