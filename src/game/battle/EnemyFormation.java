package game.battle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Stores the enemy monsters in a fixed formation of three front-row
 * positions and three back-row positions.
 *
 * Empty positions are represented by null. Monsters never change positions
 * automatically when another monster faints.
 */
public class EnemyFormation {

    /** Number of positions in each row. */
    public static final int SLOTS_PER_ROW = 3;

    /** The two rows of the enemy formation. */
    public enum Row {
        FRONT,
        BACK
    }

    /**
     * Identifies one position in the formation.
     *
     * @param row  the row containing the position
     * @param slot the position within the row, from 0 to 2
     */
    public record Position(Row row, int slot) {

        /**
         * Validates a formation position.
         */
        public Position {
            if (row == null) {
                throw new IllegalArgumentException("Formation row cannot be null.");
            }
            if (slot < 0 || slot >= SLOTS_PER_ROW) {
                throw new IllegalArgumentException("Invalid formation slot: " + slot);
            }
        }
    }

    private final BattleMonster[] frontRow;
    private final BattleMonster[] backRow;

    /**
     * Creates a formation using two fixed arrays.
     *
     * Each array must contain exactly three positions. Null positions
     * represent empty slots.
     *
     * @param frontRow three front-row positions
     * @param backRow  three back-row positions
     */
    public EnemyFormation(
            BattleMonster[] frontRow,
            BattleMonster[] backRow) {

        validateRow(frontRow, "Front");
        validateRow(backRow, "Back");

        if (isEntirelyEmpty(frontRow) && isEntirelyEmpty(backRow)) {
            throw new IllegalArgumentException(
                    "An enemy formation must contain at least one monster.");
        }

        Set<BattleMonster> uniqueMonsters =
                Collections.newSetFromMap(new IdentityHashMap<>());

        validateUniqueMonsters(frontRow, uniqueMonsters);
        validateUniqueMonsters(backRow, uniqueMonsters);

        this.frontRow = frontRow.clone();
        this.backRow = backRow.clone();
    }

    /**
     * Returns the monster at the specified position.
     *
     * @param row  formation row
     * @param slot position within the row
     * @return the monster, or null if the position is empty
     */
    public BattleMonster getEnemy(Row row, int slot) {
        validatePosition(row, slot);
        return getRow(row)[slot];
    }

    /**
     * Checks whether a position contains a monster.
     *
     * @param row  formation row
     * @param slot position within the row
     * @return true if the position is occupied
     */
    public boolean isOccupied(Row row, int slot) {
        return getEnemy(row, slot) != null;
    }

    /**
     * Checks whether the monster at a position can still fight.
     *
     * @param row  formation row
     * @param slot position within the row
     * @return true if the position contains a living monster
     */
    public boolean isAlive(Row row, int slot) {
        BattleMonster monster = getEnemy(row, slot);
        return monster != null && !monster.isFainted();
    }

    /**
     * Returns all living enemies in stable formation order:
     * front row from left to right, followed by back row from left to right.
     *
     * @return an unmodifiable list of living enemies
     */
    public List<BattleMonster> getLivingEnemies() {
        List<BattleMonster> living = new ArrayList<>();

        addLivingEnemies(frontRow, living);
        addLivingEnemies(backRow, living);

        return Collections.unmodifiableList(living);
    }

    /**
     * Returns the number of enemies that can still fight.
     *
     * @return living enemy count
     */
    public int getLivingCount() {
        return getLivingEnemies().size();
    }

    /**
     * Checks whether at least one enemy can still fight.
     *
     * @return true if a living enemy exists
     */
    public boolean hasLivingEnemies() {
        return getLivingCount() > 0;
    }

    /**
     * Checks whether every enemy has fainted.
     *
     * @return true if no living enemy remains
     */
    public boolean isDefeated() {
        return !hasLivingEnemies();
    }

    /**
     * Returns the positions that can be targeted by a move.
     *
     * Melee moves can target the front row while at least one front-row
     * enemy is alive. Once the front row is cleared, melee moves can target
     * the back row.
     *
     * Ranged moves can target any living enemy.
     *
     * @param targetType the move's targeting type
     * @return valid target positions in formation order
     */
    public List<Position> getTargetablePositions(BattleMove.TargetType targetType) {
        if (targetType == null) {
            throw new IllegalArgumentException("Target type cannot be null.");
        }

        List<Position> positions = new ArrayList<>();

        if (targetType == BattleMove.TargetType.RANGED) {
            addLivingPositions(Row.FRONT, frontRow, positions);
            addLivingPositions(Row.BACK, backRow, positions);
        } else if (hasLivingEnemiesInRow(frontRow)) {
            addLivingPositions(Row.FRONT, frontRow, positions);
        } else {
            addLivingPositions(Row.BACK, backRow, positions);
        }

        return Collections.unmodifiableList(positions);
    }

    /**
     * Converts a position into a stable index from 0 to 5.
     * Front positions use 0-2; back positions use 3-5.
     *
     * @param position formation position
     * @return stable position index
     */
    public int getPositionIndex(Position position) {
        if (position == null) {
            throw new IllegalArgumentException("Position cannot be null.");
        }

        return position.slot()
                + (position.row() == Row.BACK ? SLOTS_PER_ROW : 0);
    }

    /**
     * Converts a stable index from 0 to 5 into a formation position.
     *
     * @param index stable position index
     * @return corresponding formation position
     */
    public Position getPositionByIndex(int index) {
        if (index < 0 || index >= SLOTS_PER_ROW * 2) {
            throw new IllegalArgumentException("Invalid formation index: " + index);
        }

        return index < SLOTS_PER_ROW
                ? new Position(Row.FRONT, index)
                : new Position(Row.BACK, index - SLOTS_PER_ROW);
    }

    /**
     * Returns the backing array for the requested row.
     */
    private BattleMonster[] getRow(Row row) {
        if (row == null) {
            throw new IllegalArgumentException("Formation row cannot be null.");
        }

        return row == Row.FRONT ? frontRow : backRow;
    }

    /**
     * Validates a row array.
     */
    private void validateRow(BattleMonster[] row, String rowName) {
        if (row == null || row.length != SLOTS_PER_ROW) {
            throw new IllegalArgumentException(
                    rowName + " row must contain exactly "
                            + SLOTS_PER_ROW + " positions.");
        }
    }

    /**
     * Checks whether every position in a row is empty.
     */
    private boolean isEntirelyEmpty(BattleMonster[] row) {
        for (BattleMonster monster : row) {
            if (monster != null) {
                return false;
            }
        }

        return true;
    }

    /**
     * Ensures that the same BattleMonster object is not used in multiple slots.
     */
    private void validateUniqueMonsters(
            BattleMonster[] row,
            Set<BattleMonster> uniqueMonsters) {

        for (BattleMonster monster : row) {
            if (monster != null && !uniqueMonsters.add(monster)) {
                throw new IllegalArgumentException(
                        "The same monster cannot occupy multiple positions.");
            }
        }
    }

    /**
     * Validates a row and slot combination.
     */
    private void validatePosition(Row row, int slot) {
        if (row == null) {
            throw new IllegalArgumentException("Formation row cannot be null.");
        }
        if (slot < 0 || slot >= SLOTS_PER_ROW) {
            throw new IllegalArgumentException("Invalid formation slot: " + slot);
        }
    }

    /**
     * Adds living monsters from an array to a list.
     */
    private void addLivingEnemies(
            BattleMonster[] row,
            List<BattleMonster> destination) {

        for (BattleMonster monster : row) {
            if (monster != null && !monster.isFainted()) {
                destination.add(monster);
            }
        }
    }

    /**
     * Checks whether a row contains at least one living monster.
     */
    private boolean hasLivingEnemiesInRow(BattleMonster[] row) {
        for (BattleMonster monster : row) {
            if (monster != null && !monster.isFainted()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Adds living positions from a row to the target list.
     */
    private void addLivingPositions(
            Row row,
            BattleMonster[] monsters,
            List<Position> destination) {

        for (int slot = 0; slot < monsters.length; slot++) {
            if (monsters[slot] != null && !monsters[slot].isFainted()) {
                destination.add(new Position(row, slot));
            }
        }
    }
}