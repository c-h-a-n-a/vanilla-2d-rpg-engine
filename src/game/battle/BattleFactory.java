package game.battle;

import game.entity.MonsterType;

import java.util.List;

/**
 * Builds battle monsters, parties and enemy formations.
 * Stats are placeholders until real monster data exists.
 */
public final class BattleFactory {

    private BattleFactory() {
    }

    /**
     * Creates the player's party.
     *
     * @return the player's six-member party
     */
    public static BattleParty createPlayerParty() {
        return new BattleParty(List.of(
                create("Blaze", 40, 10, 4, 8,
                        move("Scratch", 6),
                        move("Ember", 9, BattleMove.TargetType.RANGED),
                        move("Tackle", 7),
                        move("Bite", 8)),

                create("Tide", 44, 9, 5, 7,
                        move("Splash", 5, BattleMove.TargetType.RANGED),
                        move("Bubble Beam", 9, BattleMove.TargetType.RANGED),
                        move("Tackle", 7),
                        move("Water Gun", 8, BattleMove.TargetType.RANGED)),

                create("Sprout", 42, 9, 6, 5,
                        move("Vine Whip", 8, BattleMove.TargetType.RANGED),
                        move("Leech Seed", 6, BattleMove.TargetType.RANGED),
                        move("Tackle", 7),
                        move("Razor Leaf", 9, BattleMove.TargetType.RANGED)),

                create("Zap", 34, 11, 3, 12,
                        move("Spark", 7, BattleMove.TargetType.RANGED),
                        move("Thunder Jolt", 9, BattleMove.TargetType.RANGED),
                        move("Quick Hit", 5),
                        move("Shock Wave", 8, BattleMove.TargetType.RANGED)),

                create("Pebble", 52, 8, 8, 3,
                        move("Rock Toss", 8, BattleMove.TargetType.RANGED),
                        move("Slam", 9),
                        move("Tackle", 7),
                        move("Stone Edge", 10)),

                create("Gale", 36, 10, 4, 11,
                        move("Gust", 7, BattleMove.TargetType.RANGED),
                        move("Wing Slash", 8),
                        move("Peck", 6),
                        move("Air Cutter", 9, BattleMove.TargetType.RANGED))
        ));
    }

    /**
     * Creates an enemy formation for an overworld monster type.
     *
     * The current prototype creates three enemies: two in the front row
     * and one in the back row.
     *
     * @param type the encountered overworld monster type
     * @return the enemy formation for the battle
     */
    public static EnemyFormation createEnemyFormation(MonsterType type) {
        if (type == null) {
            throw new IllegalArgumentException("Monster type cannot be null.");
        }

        BattleMonster frontLeft = createEnemy(type, "A", 90, 12, 3, 10);
        BattleMonster frontRight = createEnemy(type, "B", 90, 12, 3, 10);
        BattleMonster backCenter = createEnemy(type, "C", 75, 10, 2, 8);

        return new EnemyFormation(
                new BattleMonster[] {
                        frontLeft,
                        null,
                        frontRight
                },
                new BattleMonster[] {
                        null,
                        backCenter,
                        null
                }
        );
    }

    /**
     * Creates one enemy for compatibility with older single-enemy callers.
     *
     * @param type monster type
     * @return a single battle monster
     */
    public static BattleMonster createEnemy(MonsterType type) {
        if (type == null) {
            throw new IllegalArgumentException("Monster type cannot be null.");
        }

        return createEnemy(type, "A", 90, 12, 3, 10);
    }

    /**
     * Creates one enemy with the default moves.
     *
     * @param type monster type
     * @param suffix display suffix used to distinguish enemies
     * @param hp maximum HP
     * @param attack attack stat
     * @param defense defense stat
     * @param speed speed stat
     * @return a new battle monster
     */
    private static BattleMonster createEnemy(
            MonsterType type,
            String suffix,
            int hp,
            int attack,
            int defense,
            int speed) {

        return create(
                type.getLabel() + " " + suffix,
                hp,
                attack,
                defense,
                speed,
                move("Bite", 5),
                move("Claw", 4),
                move("Howl", 1, BattleMove.TargetType.RANGED),
                move("Lunge", 6)
        );
    }

    /**
     * Shorthand for building a monster from four moves.
     *
     * @param name monster name
     * @param maxHp maximum HP
     * @param attack attack stat
     * @param defense defense stat
     * @param speed speed stat
     * @param moves monster moves
     * @return a new battle monster
     */
    private static BattleMonster create(
            String name,
            int maxHp,
            int attack,
            int defense,
            int speed,
            BattleMove... moves) {

        return new BattleMonster(name, maxHp, attack, defense, speed, moves);
    }

    /**
     * Creates a melee move.
     *
     * @param name move name
     * @param power move power
     * @return a new melee move
     */
    private static BattleMove move(String name, int power) {
        return new BattleMove(name, power);
    }

    /**
     * Creates a move with a specified target type.
     *
     * @param name move name
     * @param power move power
     * @param targetType targeting type
     * @return a new battle move
     */
    private static BattleMove move(
            String name,
            int power,
            BattleMove.TargetType targetType) {

        return new BattleMove(name, power, targetType);
    }
}