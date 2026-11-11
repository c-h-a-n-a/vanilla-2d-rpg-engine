package game.battle;

import java.util.List;

/**
 * Holds one battle and advances it one round at a time.
 * The player picks a move; the enemy picks its own; the turn manager orders
 * them by speed and both are resolved. No rendering code lives here.
 */
public class BattleState {

    /** Where the battle currently stands. */
    public enum Phase {
        PLAYER_TURN,
        VICTORY,
        DEFEAT
    }

    private final BattleMonster playerMonster;
    private final BattleMonster enemyMonster;

    private final BattleTurnManager turnManager = new BattleTurnManager();
    private final EnemyAI enemyAI;

    private Phase phase;
    private String lastMessage;

    /** Creates a battle with the default random enemy AI. */
    public BattleState(BattleMonster playerMonster, BattleMonster enemyMonster) {
        this(playerMonster, enemyMonster, new EnemyAI());
    }

    /** Creates a battle with a supplied enemy AI (useful for deterministic tests). */
    public BattleState(
            BattleMonster playerMonster,
            BattleMonster enemyMonster,
            EnemyAI enemyAI) {

        this.playerMonster = playerMonster;
        this.enemyMonster = enemyMonster;
        this.enemyAI = enemyAI;

        this.phase = Phase.PLAYER_TURN;
        this.lastMessage = "Choose a move!";
    }

    /**
     * Plays one full round using the player's chosen move slot.
     * Does nothing if the battle is over or the slot is invalid.
     */
    public void executePlayerMove(int moveIndex) {
        if (phase != Phase.PLAYER_TURN) {
            return;
        }
        if (moveIndex < 0 || moveIndex >= BattleMonster.MOVE_COUNT) {
            return;
        }

        BattleAction playerAction = new BattleAction(
                playerMonster, enemyMonster, playerMonster.getMove(moveIndex));

        BattleAction enemyAction = new BattleAction(
                enemyMonster, playerMonster,
                enemyMonster.getMove(enemyAI.chooseMoveIndex(enemyMonster)));

        resolveRound(turnManager.orderActions(playerAction, enemyAction));
    }

    /**
     * Resolves the ordered actions of a round. If a monster faints, the round
     * ends immediately, so a fainted monster never gets to act.
     */
    private void resolveRound(List<BattleAction> orderedActions) {
        StringBuilder log = new StringBuilder();

        for (BattleAction action : orderedActions) {
            BattleMonster defender = action.defender();

            int damage = DamageCalculator.calculate(
                    action.attacker(), defender, action.move());
            defender.takeDamage(damage);

            appendLine(log, action.attacker().getName()
                    + " used " + action.move().getName()
                    + "! It dealt " + damage + " damage.");

            if (defender.isFainted()) {
                boolean enemyFainted = defender == enemyMonster;
                phase = enemyFainted ? Phase.VICTORY : Phase.DEFEAT;

                appendLine(log, defender.getName() + " fainted! "
                        + (enemyFainted ? "You won!" : "You lost..."));
                break;
            }
        }

        lastMessage = log.toString();
    }

    /** Appends a line to the log, adding a newline separator when needed. */
    private void appendLine(StringBuilder log, String line) {
        if (log.length() > 0) {
            log.append('\n');
        }
        log.append(line);
    }

    /** Returns the player's monster. */
    public BattleMonster getPlayerMonster() {
        return playerMonster;
    }

    /** Returns the enemy monster. */
    public BattleMonster getEnemyMonster() {
        return enemyMonster;
    }

    /** Returns the current battle phase. */
    public Phase getPhase() {
        return phase;
    }

    /** Returns the message(s) from the last round, lines separated by '\n'. */
    public String getLastMessage() {
        return lastMessage;
    }
}