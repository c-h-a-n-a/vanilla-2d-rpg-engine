package game.battle;

import game.entity.MonsterType;

/** Builds battle monsters. Stats here are placeholders until real monster data exists. */
public final class BattleFactory {

    private BattleFactory() {
    }

    /** Creates the player's starter monster. */
    public static BattleMonster createPlayerMonster() {
        return new BattleMonster("Blaze", 40, 10, 4, 8, new BattleMove[]{
                new BattleMove("Scratch", 6),
                new BattleMove("Ember", 9),
                new BattleMove("Tackle", 7),
                new BattleMove("Bite", 8)
        });
    }

    /**
     * Creates the enemy for an overworld monster type.
     * Later this can switch on the type for per-species stats and moves.
     */
    public static BattleMonster createEnemy(MonsterType type) {
        // Speed 10 beats the player's 8, so the wolf acts first (visible turn order).
        return new BattleMonster(type.getLabel(), 45, 8, 3, 10, new BattleMove[]{
                new BattleMove("Bite", 5),
                new BattleMove("Claw", 4),
                new BattleMove("Howl", 1),
                new BattleMove("Lunge", 6)
        });
    }
}