package game.battle;

import java.util.Random;

/** Chooses the enemy's move. Currently a random pick; smarter AI comes in Milestone 4. */
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
}