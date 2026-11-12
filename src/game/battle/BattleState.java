package game.battle;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds one battle (a player party against one enemy) and advances it one
 * round at a time. No rendering or input code lives here.
 */
public class BattleState {

    /** Where the battle currently stands. */
    public enum Phase {
        /** The player is choosing a move for each active monster. */
        COMMAND_SELECTION,
        /** An active monster fainted; the player must send in a replacement. */
        FORCED_SWITCH,
        VICTORY,
        DEFEAT
    }

    private final BattleParty playerParty;
    private final BattleMonster enemyMonster;

    private final BattleTurnManager turnManager = new BattleTurnManager();
    private final EnemyAI enemyAI;

    private Phase phase = Phase.COMMAND_SELECTION;
    private String lastMessage;

    /** Creates a battle with the default random enemy AI. */
    public BattleState(BattleParty playerParty, BattleMonster enemyMonster) {
        this(playerParty, enemyMonster, new EnemyAI());
    }

    /** Creates a battle with a supplied enemy AI (useful for deterministic tests). */
    public BattleState(
            BattleParty playerParty,
            BattleMonster enemyMonster,
            EnemyAI enemyAI) {

        this.playerParty = playerParty;
        this.enemyMonster = enemyMonster;
        this.enemyAI = enemyAI;

        this.lastMessage = "A wild " + enemyMonster.getName() + " appeared!";
    }

    /** Returns true if the given active slot holds a monster that can act. */
    public boolean canSlotAct(int slot) {
        return playerParty.isSlotAlive(slot);
    }

    /**
     * Plays one full round. The enemy picks its action, then all actions are
     * ordered by speed and resolved. A monster that faints before its turn
     * loses its action. Does nothing if the phase is wrong or a slot that can
     * act has no valid move chosen.
     *
     * @param moveIndices chosen move per party slot; ignored for slots that cannot act
     */
    public void executeRound(int[] moveIndices) {
        if (phase != Phase.COMMAND_SELECTION
                || moveIndices == null
                || moveIndices.length != BattleParty.ACTIVE_SLOTS) {
            return;
        }

        // Player actions first (slot order), then the enemy: this is the speed tie-break order.
        List<BattleAction> actions = new ArrayList<>();

        for (int slot = 0; slot < BattleParty.ACTIVE_SLOTS; slot++) {
            if (!canSlotAct(slot)) {
                continue;
            }

            int moveIndex = moveIndices[slot];
            if (moveIndex < 0 || moveIndex >= BattleMonster.MOVE_COUNT) {
                return; // incomplete selection: reject the whole round
            }

            BattleMonster attacker = playerParty.getActive(slot);
            actions.add(new BattleAction(
                    attacker, enemyMonster, attacker.getMove(moveIndex)));
        }

        int targetSlot = enemyAI.chooseTargetSlot(playerParty);
        if (targetSlot >= 0) {
            BattleMove enemyMove =
                    enemyMonster.getMove(enemyAI.chooseMoveIndex(enemyMonster));

            actions.add(new BattleAction(
                    enemyMonster, playerParty.getActive(targetSlot), enemyMove));
        }

        resolveRound(turnManager.orderActions(actions));
    }

    /**
     * Resolves the ordered actions, builds the message log and moves to the
     * next phase (victory, defeat, forced switch or a new command phase).
     */
    private void resolveRound(List<BattleAction> orderedActions) {
        List<String> log = new ArrayList<>();

        for (BattleAction action : orderedActions) {
            // Fainted earlier this round, so this action is lost.
            if (action.attacker().isFainted()) {
                continue;
            }

            BattleMonster defender = action.defender();

            int damage = DamageCalculator.calculate(
                    action.attacker(), defender, action.move());
            defender.takeDamage(damage);

            log.add(action.attacker().getName()
                    + " used " + action.move().getName()
                    + " on " + defender.getName()
                    + "! (" + damage + " damage)");

            if (defender.isFainted()) {
                log.add(defender.getName() + " fainted!");
            }

            if (enemyMonster.isFainted() || playerParty.isDefeated()) {
                break;
            }
        }

        if (enemyMonster.isFainted()) {
            phase = Phase.VICTORY;
            log.add("You won!");
        } else if (playerParty.isDefeated()) {
            phase = Phase.DEFEAT;
            log.add("You lost...");
        } else {
            phase = nextCommandPhase();
        }

        lastMessage = String.join("\n", log);
    }

    /**
     * Swaps an active monster with a reserve monster. This is free: it never
     * consumes an action, and the incoming monster inherits the slot's action
     * for the round. Allowed during command selection (any slot) and during a
     * forced switch (only the slot that needs a replacement).
     *
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
        if (phase == Phase.FORCED_SWITCH && slot != getSlotNeedingReplacement()) {
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
     * Returns the first active slot whose monster fainted while a living
     * reserve monster is available, or -1 if no replacement is needed.
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

    /** Returns FORCED_SWITCH if a replacement is needed, otherwise COMMAND_SELECTION. */
    private Phase nextCommandPhase() {
        return getSlotNeedingReplacement() >= 0
                ? Phase.FORCED_SWITCH
                : Phase.COMMAND_SELECTION;
    }

    /** Returns the player's party. */
    public BattleParty getPlayerParty() {
        return playerParty;
    }

    /** Returns the enemy monster. */
    public BattleMonster getEnemyMonster() {
        return enemyMonster;
    }

    /** Returns the current battle phase. */
    public Phase getPhase() {
        return phase;
    }

    /** Returns the message(s) from the last event, lines separated by '\n'. */
    public String getLastMessage() {
        return lastMessage;
    }
}