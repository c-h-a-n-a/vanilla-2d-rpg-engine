package game.battle;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds one battle between the player's party and an enemy formation.
 * Contains no rendering or input code.
 */
public class BattleState {

    /** Where the battle currently stands. */
    public enum Phase {
        /** The player is choosing moves and targets. */
        COMMAND_SELECTION,

        /** An active player monster fainted and needs a replacement. */
        FORCED_SWITCH,

        VICTORY,
        DEFEAT
    }

    private final BattleParty playerParty;
    private final EnemyFormation enemyFormation;

    private final BattleTurnManager turnManager = new BattleTurnManager();
    private final EnemyAI enemyAI;

    private Phase phase = Phase.COMMAND_SELECTION;
    private String lastMessage;

    /**
     * Creates a battle with the default random enemy AI.
     *
     * @param playerParty player's party
     * @param enemyFormation enemy formation
     */
    public BattleState(
            BattleParty playerParty,
            EnemyFormation enemyFormation) {

        this(playerParty, enemyFormation, new EnemyAI());
    }

    /**
     * Creates a battle with a supplied enemy AI.
     *
     * @param playerParty player's party
     * @param enemyFormation enemy formation
     * @param enemyAI enemy decision logic
     */
    public BattleState(
            BattleParty playerParty,
            EnemyFormation enemyFormation,
            EnemyAI enemyAI) {

        if (playerParty == null) {
            throw new IllegalArgumentException("Player party cannot be null.");
        }
        if (enemyFormation == null || !enemyFormation.hasLivingEnemies()) {
            throw new IllegalArgumentException(
                    "Enemy formation must contain a living monster.");
        }
        if (enemyAI == null) {
            throw new IllegalArgumentException("Enemy AI cannot be null.");
        }

        this.playerParty = playerParty;
        this.enemyFormation = enemyFormation;
        this.enemyAI = enemyAI;

        this.lastMessage = createEncounterMessage();
    }

    /**
     * Compatibility constructor for a battle containing one enemy.
     *
     * @param playerParty player's party
     * @param enemyMonster single enemy
     */
    public BattleState(BattleParty playerParty, BattleMonster enemyMonster) {
        this(playerParty, createSingleEnemyFormation(enemyMonster));
    }

    /**
     * Creates a one-enemy formation for compatibility with older callers.
     *
     * @param playerParty player's party
     * @param enemyMonster single enemy
     * @param enemyAI enemy AI
     */
    public BattleState(
            BattleParty playerParty,
            BattleMonster enemyMonster,
            EnemyAI enemyAI) {

        this(playerParty, createSingleEnemyFormation(enemyMonster), enemyAI);
    }

    /**
     * Creates a formation containing one enemy in the front-left position.
     *
     * @param enemyMonster enemy to place in the formation
     * @return a one-enemy formation
     */
    private static EnemyFormation createSingleEnemyFormation(
            BattleMonster enemyMonster) {

        if (enemyMonster == null) {
            throw new IllegalArgumentException("Enemy monster cannot be null.");
        }

        return new EnemyFormation(
                new BattleMonster[] { enemyMonster, null, null },
                new BattleMonster[] { null, null, null }
        );
    }

    /**
     * Creates the initial encounter message.
     *
     * @return encounter message
     */
    private String createEncounterMessage() {
        List<String> names = new ArrayList<>();

        for (BattleMonster enemy : enemyFormation.getLivingEnemies()) {
            names.add(enemy.getName());
        }

        return "You encountered " + String.join(", ", names) + "!";
    }

    /**
     * Returns true if the given active player slot can act.
     *
     * @param slot player active slot
     * @return true if the slot contains a living monster
     */
    public boolean canSlotAct(int slot) {
        return playerParty.isSlotAlive(slot);
    }

    /**
     * Returns the positions that can be targeted by the given move.
     *
     * @param move selected move
     * @return valid enemy positions
     */
    public List<EnemyFormation.Position> getTargetablePositions(BattleMove move) {
        if (move == null) {
            return List.of();
        }

        return enemyFormation.getTargetablePositions(move.getTargetType());
    }

    /**
     * Plays one full round.
     *
     * Each living player monster uses its selected move against its selected
     * enemy position. Every living enemy then receives one AI-selected action.
     * All actions are ordered by attacker speed.
     *
     * @param moveIndices selected move for each active player slot
     * @param targetIndices selected enemy position for each active player slot
     */
    public void executeRound(int[] moveIndices, int[] targetIndices) {
        if (phase != Phase.COMMAND_SELECTION
                || moveIndices == null
                || targetIndices == null
                || moveIndices.length != BattleParty.ACTIVE_SLOTS
                || targetIndices.length != BattleParty.ACTIVE_SLOTS) {
            return;
        }

        List<BattleAction> actions = new ArrayList<>();

        // Build player actions in active-slot order.
        for (int slot = 0; slot < BattleParty.ACTIVE_SLOTS; slot++) {
            if (!canSlotAct(slot)) {
                continue;
            }

            int moveIndex = moveIndices[slot];
            int targetIndex = targetIndices[slot];

            if (moveIndex < 0 || moveIndex >= BattleMonster.MOVE_COUNT) {
                return;
            }

            BattleMonster attacker = playerParty.getActive(slot);
            BattleMove move = attacker.getMove(moveIndex);

            if (!isValidTarget(move, targetIndex)) {
                return;
            }

            EnemyFormation.Position position =
                    enemyFormation.getPositionByIndex(targetIndex);

            BattleMonster defender = enemyFormation.getEnemy(
                    position.row(),
                    position.slot());

            actions.add(new BattleAction(attacker, defender, move));
        }

        // Every living enemy gets one action.
        for (BattleMonster enemy : enemyFormation.getLivingEnemies()) {
            int targetSlot = enemyAI.chooseTargetSlot(playerParty);

            if (targetSlot < 0) {
                continue;
            }

            int moveIndex = enemyAI.chooseMoveIndex(enemy);

            BattleMove enemyMove = enemy.getMove(moveIndex);

            actions.add(new BattleAction(
                    enemy,
                    playerParty.getActive(targetSlot),
                    enemyMove
            ));
        }

        resolveRound(turnManager.orderActions(actions));
    }

    /**
     * Checks whether a target index is valid for a move.
     *
     * @param move selected move
     * @param targetIndex formation position index
     * @return true if the target is currently legal
     */
    private boolean isValidTarget(BattleMove move, int targetIndex) {
        if (targetIndex < 0 || targetIndex >= EnemyFormation.SLOTS_PER_ROW * 2) {
            return false;
        }

        EnemyFormation.Position position =
                enemyFormation.getPositionByIndex(targetIndex);

        if (!enemyFormation.isAlive(position.row(), position.slot())) {
            return false;
        }

        return enemyFormation.getTargetablePositions(move.getTargetType())
                .contains(position);
    }

    /**
     * Resolves ordered actions and determines the next battle phase.
     *
     * An action is skipped if its attacker or target has already fainted.
     *
     * @param orderedActions actions ordered by speed
     */
    private void resolveRound(List<BattleAction> orderedActions) {
        List<String> log = new ArrayList<>();

        for (BattleAction action : orderedActions) {
            if (action.attacker().isFainted()
                    || action.defender().isFainted()) {
                continue;
            }

            int damage = DamageCalculator.calculate(
                    action.attacker(),
                    action.defender(),
                    action.move()
            );

            action.defender().takeDamage(damage);

            log.add(action.attacker().getName()
                    + " used " + action.move().getName()
                    + " on " + action.defender().getName()
                    + "! (" + damage + " damage)");

            if (action.defender().isFainted()) {
                log.add(action.defender().getName() + " fainted!");
            }

            if (enemyFormation.isDefeated() || playerParty.isDefeated()) {
                break;
            }
        }

        if (enemyFormation.isDefeated()) {
            phase = Phase.VICTORY;
            log.add("You won!");
        } else if (playerParty.isDefeated()) {
            phase = Phase.DEFEAT;
            log.add("You lost...");
        } else {
            phase = nextCommandPhase();
        }

        lastMessage = log.isEmpty()
                ? "No actions were performed."
                : String.join("\n", log);
    }

    /**
     * Swaps an active monster with a reserve monster.
     *
     * Switching is free and does not consume the active slot's action.
     *
     * @param slot active party slot
     * @param reserveIndex reserve monster index
     * @return true if the switch happened
     */
    public boolean switchActiveMonster(int slot, int reserveIndex) {
        if (phase != Phase.COMMAND_SELECTION && phase != Phase.FORCED_SWITCH) {
            return false;
        }

        if (slot < 0 || slot >= BattleParty.ACTIVE_SLOTS
                || playerParty.getActive(slot) == null
                || !playerParty.canSwitchTo(reserveIndex)) {
            return false;
        }

        if (phase == Phase.FORCED_SWITCH
                && slot != getSlotNeedingReplacement()) {
            return false;
        }

        BattleMonster outgoing = playerParty.getActive(slot);
        BattleMonster incoming = playerParty.getReserveMember(reserveIndex);

        playerParty.switchSlot(slot, reserveIndex);

        lastMessage = outgoing.isFainted()
                ? "Go, " + incoming.getName() + "!"
                : "Come back, " + outgoing.getName()
                + "! Go, " + incoming.getName() + "!";

        if (phase == Phase.FORCED_SWITCH) {
            phase = nextCommandPhase();
        }

        return true;
    }

    /**
     * Returns the first active slot that needs a replacement.
     *
     * @return slot index, or -1 if no replacement is needed
     */
    public int getSlotNeedingReplacement() {
        if (!playerParty.hasLivingReserve()) {
            return -1;
        }

        for (int slot = 0; slot < BattleParty.ACTIVE_SLOTS; slot++) {
            BattleMonster monster = playerParty.getActive(slot);

            if (monster != null && monster.isFainted()) {
                return slot;
            }
        }

        return -1;
    }

    /**
     * Determines whether the next phase requires a forced switch.
     *
     * @return next command phase
     */
    private Phase nextCommandPhase() {
        return getSlotNeedingReplacement() >= 0
                ? Phase.FORCED_SWITCH
                : Phase.COMMAND_SELECTION;
    }

    /** Returns the player's party. */
    public BattleParty getPlayerParty() {
        return playerParty;
    }

    /** Returns the enemy formation. */
    public EnemyFormation getEnemyFormation() {
        return enemyFormation;
    }

    /**
     * Returns the first living enemy.
     *
     * Kept for compatibility with older single-enemy callers.
     *
     * @return first living enemy, or null if none remain
     */
    public BattleMonster getEnemyMonster() {
        List<BattleMonster> living = enemyFormation.getLivingEnemies();
        return living.isEmpty() ? null : living.get(0);
    }

    /** Returns the current battle phase. */
    public Phase getPhase() {
        return phase;
    }

    /** Returns the most recent battle message. */
    public String getLastMessage() {
        return lastMessage;
    }
}