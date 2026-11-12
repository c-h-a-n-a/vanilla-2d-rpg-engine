package game.state;

import game.battle.BattleMonster;
import game.battle.BattleParty;
import game.battle.BattleState;
import game.input.KeyboardInput;

import java.awt.event.KeyEvent;
import java.util.Arrays;

/**
 * Turns keyboard input into battle commands and keeps the battle menu state
 * (which slot is choosing, cursor position, queued moves). It draws nothing;
 * the renderer reads its getters.
 */
public class BattleController {

    /** What the command menu is currently showing. */
    public enum Mode {
        MOVE_SELECT,
        SWITCH_SELECT
    }

    private static final int NO_MOVE = -1;

    private final KeyboardInput input;
    private final BattleState state;
    private final int[] chosenMoves = new int[BattleParty.ACTIVE_SLOTS];

    private Mode mode = Mode.MOVE_SELECT;
    private int currentSlot;
    private int cursor;

    private boolean finished;
    private boolean playerWon;

    /** Creates a controller for one battle. */
    public BattleController(KeyboardInput input, BattleState state) {
        this.input = input;
        this.state = state;
        beginRound();
    }

    /** Processes this frame's input. Call once per update while in battle. */
    public void update() {
        // Temporary escape hatch for testing; the real Run command comes later.
        if (input.consumeJustPressed(KeyEvent.VK_ESCAPE)) {
            finished = true;
            playerWon = false;
            return;
        }

        switch (state.getPhase()) {
            case VICTORY, DEFEAT -> updateBattleOver();
            case FORCED_SWITCH -> updateSwitchSelection(true);
            case COMMAND_SELECTION -> {
                if (mode == Mode.SWITCH_SELECT) {
                    updateSwitchSelection(false);
                } else {
                    updateMoveSelection();
                }
            }
        }
    }

    /** Waits for confirmation on the result screen, then marks the battle finished. */
    private void updateBattleOver() {
        if (confirmPressed()) {
            finished = true;
            playerWon = state.getPhase() == BattleState.Phase.VICTORY;
        }
    }

    /**
     * Handles the move menu for the current slot: pick a move, open the
     * switch list, or step back to the previous slot. Once every acting slot
     * has a move, the round is executed.
     */
    private void updateMoveSelection() {
        cursor = navigate(BattleMonster.MOVE_COUNT);

        if (switchPressed()) {
            if (state.getPlayerParty().hasLivingReserve()) {
                mode = Mode.SWITCH_SELECT;
                cursor = firstSwitchableReserve();
            }
        } else if (backPressed()) {
            goBackOneSlot();
        } else if (confirmPressed()) {
            chosenMoves[currentSlot] = cursor;

            int next = nextActingSlot(currentSlot + 1);
            if (next < 0) {
                state.executeRound(chosenMoves);
                beginRound();
            } else {
                currentSlot = next;
                cursor = 0;
            }
        }
    }

    /**
     * Handles the reserve list. During a normal turn the incoming monster
     * inherits the slot's pending action, so we simply return to the move
     * menu. During a forced switch the round is over, so a new round begins.
     *
     * @param forced true when a fainted monster must be replaced
     */
    private void updateSwitchSelection(boolean forced) {
        int slot = forced ? state.getSlotNeedingReplacement() : currentSlot;

        cursor = navigate(state.getPlayerParty().getReserveCount());

        if (!forced && backPressed()) {
            mode = Mode.MOVE_SELECT;
            cursor = 0;
            return;
        }

        if (confirmPressed() && state.switchActiveMonster(slot, cursor)) {
            if (forced) {
                beginRound();
            } else {
                mode = Mode.MOVE_SELECT;
                cursor = 0;
            }
        }
    }

    /** Resets the per-round selection state and points at the first slot that can act. */
    private void beginRound() {
        Arrays.fill(chosenMoves, NO_MOVE);
        mode = Mode.MOVE_SELECT;

        int first = nextActingSlot(0);
        currentSlot = Math.max(first, 0);

        cursor = state.getPhase() == BattleState.Phase.FORCED_SWITCH
                ? firstSwitchableReserve()
                : 0;
    }

    /** Returns to the previous acting slot and clears its queued move. */
    private void goBackOneSlot() {
        for (int slot = currentSlot - 1; slot >= 0; slot--) {
            if (state.canSlotAct(slot)) {
                chosenMoves[slot] = NO_MOVE;
                currentSlot = slot;
                cursor = 0;
                return;
            }
        }
    }

    /** Returns the first slot at or after {@code from} that can act, or -1. */
    private int nextActingSlot(int from) {
        for (int slot = from; slot < BattleParty.ACTIVE_SLOTS; slot++) {
            if (state.canSlotAct(slot)) {
                return slot;
            }
        }
        return -1;
    }

    /** Returns the first reserve index that can be sent in (0 if none). */
    private int firstSwitchableReserve() {
        BattleParty party = state.getPlayerParty();

        for (int i = 0; i < party.getReserveCount(); i++) {
            if (party.canSwitchTo(i)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Moves the cursor on a 2x2 grid using this frame's direction keys.
     * Cells beyond {@code count} are skipped.
     */
    private int navigate(int count) {
        int column = Math.max(0, Math.min(1, cursor % 2 + horizontalInput()));
        int row = Math.max(0, Math.min(1, cursor / 2 + verticalInput()));

        int candidate = row * 2 + column;
        return candidate < count ? candidate : cursor;
    }

    /** Returns -1 for left, 1 for right, 0 for none (consumes the key presses). */
    private int horizontalInput() {
        int direction = 0;

        if (input.consumeJustPressed(KeyEvent.VK_LEFT)
                | input.consumeJustPressed(KeyEvent.VK_A)) {
            direction--;
        }
        if (input.consumeJustPressed(KeyEvent.VK_RIGHT)
                | input.consumeJustPressed(KeyEvent.VK_D)) {
            direction++;
        }
        return direction;
    }

    /** Returns -1 for up, 1 for down, 0 for none (consumes the key presses). */
    private int verticalInput() {
        int direction = 0;

        if (input.consumeJustPressed(KeyEvent.VK_UP)
                | input.consumeJustPressed(KeyEvent.VK_W)) {
            direction--;
        }
        if (input.consumeJustPressed(KeyEvent.VK_DOWN)
                | input.consumeJustPressed(KeyEvent.VK_S)) {
            direction++;
        }
        return direction;
    }

    /** True if E or Enter was pressed this frame. */
    private boolean confirmPressed() {
        return input.consumeJustPressed(KeyEvent.VK_E)
                | input.consumeJustPressed(KeyEvent.VK_ENTER);
    }

    /** True if X or Backspace was pressed this frame. */
    private boolean backPressed() {
        return input.consumeJustPressed(KeyEvent.VK_X)
                | input.consumeJustPressed(KeyEvent.VK_BACK_SPACE);
    }

    /** True if Q was pressed this frame. */
    private boolean switchPressed() {
        return input.consumeJustPressed(KeyEvent.VK_Q);
    }

    /** Returns the battle being controlled. */
    public BattleState getState() {
        return state;
    }

    /** Returns what the menu is showing (a forced switch always shows the reserve list). */
    public Mode getMode() {
        return state.getPhase() == BattleState.Phase.FORCED_SWITCH
                ? Mode.SWITCH_SELECT
                : mode;
    }

    /** Returns the slot being commanded, or the slot awaiting a replacement. */
    public int getCurrentSlot() {
        return state.getPhase() == BattleState.Phase.FORCED_SWITCH
                ? state.getSlotNeedingReplacement()
                : currentSlot;
    }

    /** Returns the highlighted grid cell. */
    public int getCursor() {
        return cursor;
    }

    /** Returns the move queued for a slot this round, or -1 if none yet. */
    public int getChosenMove(int slot) {
        return chosenMoves[slot];
    }

    /** True if an earlier acting slot exists to step back to. */
    public boolean canGoBack() {
        for (int slot = currentSlot - 1; slot >= 0; slot--) {
            if (state.canSlotAct(slot)) {
                return true;
            }
        }
        return false;
    }

    /** True once the player has left the battle. */
    public boolean isFinished() {
        return finished;
    }

    /** True if the battle ended in victory (only meaningful once finished). */
    public boolean isPlayerWon() {
        return playerWon;
    }
}