package game.battle;

import game.entity.MonsterType;

import java.util.List;

/** Builds battle monsters and parties. Stats are placeholders until real data exists. */
public final class BattleFactory {

    private BattleFactory() {
    }

    /**
     * Creates the player's party. Hard-coded for now; this is where a real
     * party system will plug in later.
     */
    public static BattleParty createPlayerParty() {
        return new BattleParty(List.of(
                create("Blaze", 40, 10, 4, 8,
                        move("Scratch", 6), move("Ember", 9),
                        move("Tackle", 7), move("Bite", 8)),
                create("Tide", 44, 9, 5, 7,
                        move("Splash", 5), move("Bubble Beam", 9),
                        move("Tackle", 7), move("Water Gun", 8)),
                create("Sprout", 42, 9, 6, 5,
                        move("Vine Whip", 8), move("Leech Seed", 6),
                        move("Tackle", 7), move("Razor Leaf", 9)),
                create("Zap", 34, 11, 3, 12,
                        move("Spark", 7), move("Thunder Jolt", 9),
                        move("Quick Hit", 5), move("Shock Wave", 8)),
                create("Pebble", 52, 8, 8, 3,
                        move("Rock Toss", 8), move("Slam", 9),
                        move("Tackle", 7), move("Stone Edge", 10)),
                create("Gale", 36, 10, 4, 11,
                        move("Gust", 7), move("Wing Slash", 8),
                        move("Peck", 6), move("Air Cutter", 9))
        ));
    }

    /**
     * Creates the enemy for an overworld monster type.
     * Later this can switch on the type for per-species stats and moves.
     */
    public static BattleMonster createEnemy(MonsterType type) {
        return create(type.getLabel(), 90, 12, 3, 10,
                move("Bite", 5), move("Claw", 4),
                move("Howl", 1), move("Lunge", 6));
    }

    /** Shorthand for building a monster from four moves. */
    private static BattleMonster create(
            String name, int maxHp, int attack, int defense, int speed,
            BattleMove... moves) {
        return new BattleMonster(name, maxHp, attack, defense, speed, moves);
    }

    /** Shorthand for building a move. */
    private static BattleMove move(String name, int power) {
        return new BattleMove(name, power);
    }
}