package game.state;

import game.battle.BattleMonster;
import game.battle.BattleMove;
import game.battle.BattleParty;
import game.battle.BattleState;
import game.battle.EnemyFormation;
import game.input.KeyboardInput;

import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.List;

/**
 * Converts keyboard input into battle commands and keeps the battle menu
 * state. The renderer reads its getters to display the current selection.
 */
public class BattleController {

    /** What the command menu is currently showing. */
    public enum Mode {
        MOVE_SELECT,
        TARGET_SELECT,
        SWITCH_SELECT
    }

    private static final int NO_SELECTION = -1;

    private final KeyboardInput input;
    private final BattleState state;

    private final int[] chosenMoves =
            new int[BattleParty.ACTIVE_SLOTS];

    private final int[] chosenTargets =
            new int[BattleParty.ACTIVE_SLOTS];

    private Mode mode = Mode.MOVE_SELECT;

    private int currentSlot;
    private int cursor;
    private int targetCursor;

    private boolean finished;
    private boolean playerWon;

    /**
     * Creates a controller for one battle.
     *
     * @param input keyboard input
     * @param state battle state
     */
    public BattleController(KeyboardInput input, BattleState state) {
        if (input == null || state == null) {
            throw new IllegalArgumentException(
                    "Input and battle state cannot be null.");
        }

        this.input = input;
        this.state = state;

        beginRound();
    }

    /**
     * Processes one frame of battle input.
     */
    public void update() {
        if (input.consumeJustPressed(KeyEvent.VK_ESCAPE)) {
            finished = true;
            playerWon = false;
            return;
        }

        switch (state.getPhase()) {
            case VICTORY, DEFEAT -> updateBattleOver();

            case FORCED_SWITCH -> updateSwitchSelection(true);

            case COMMAND_SELECTION -> {
                switch (mode) {
                    case MOVE_SELECT -> updateMoveSelection();
                    case TARGET_SELECT -> updateTargetSelection();
                    case SWITCH_SELECT -> updateSwitchSelection(false);
                }
            }
        }
    }

    /**
     * Waits for confirmation on the result screen.
     */
    private void updateBattleOver() {
        if (confirmPressed()) {
            finished = true;
            playerWon = state.getPhase() == BattleState.Phase.VICTORY;
        }
    }

    /**
     * Handles the move-selection menu.
     */
    private void updateMoveSelection() {
        cursor = navigate(cursor, BattleMonster.MOVE_COUNT, 2);

        if (switchPressed()) {
            if (state.getPlayerParty().hasLivingReserve()) {
                mode = Mode.SWITCH_SELECT;
                cursor = firstSwitchableReserve();
            }

            return;
        }

        if (backPressed()) {
            goBackOneSlot();
            return;
        }

        if (confirmPressed()) {
            chosenMoves[currentSlot] = cursor;
            chosenTargets[currentSlot] = NO_SELECTION;

            BattleMove selectedMove =
                    state.getPlayerParty()
                            .getActive(currentSlot)
                            .getMove(cursor);

            List<EnemyFormation.Position> validTargets =
                    state.getTargetablePositions(selectedMove);

            if (validTargets.isEmpty()) {
                chosenMoves[currentSlot] = NO_SELECTION;
                return;
            }

            targetCursor =
                    state.getEnemyFormation().getPositionIndex(validTargets.get(0));

            mode = Mode.TARGET_SELECT;
        }
    }

    /**
     * Handles enemy target selection for the current player monster.
     */
    private void updateTargetSelection() {
        BattleMove selectedMove =
                state.getPlayerParty()
                        .getActive(currentSlot)
                        .getMove(chosenMoves[currentSlot]);

        List<EnemyFormation.Position> validTargets =
                state.getTargetablePositions(selectedMove);

        if (validTargets.isEmpty()) {
            mode = Mode.MOVE_SELECT;
            return;
        }

        targetCursor = navigateTargets(targetCursor, validTargets);

        if (backPressed()) {
            mode = Mode.MOVE_SELECT;
            cursor = chosenMoves[currentSlot];
            return;
        }

        if (confirmPressed()) {
            chosenTargets[currentSlot] = targetCursor;

            int next = nextActingSlot(currentSlot + 1);

            if (next < 0) {
                state.executeRound(chosenMoves, chosenTargets);
                beginRound();
            } else {
                currentSlot = next;
                cursor = 0;
                mode = Mode.MOVE_SELECT;
            }
        }
    }

    /**
     * Handles the reserve list.
     *
     * During a normal turn, the incoming monster inherits the active slot's
     * action. During a forced switch, a new round begins after switching.
     *
     * @param forced true when a fainted monster must be replaced
     */
    private void updateSwitchSelection(boolean forced) {
        int slot = forced
                ? state.getSlotNeedingReplacement()
                : currentSlot;

        cursor = navigate(
                cursor,
                state.getPlayerParty().getReserveCount(),
                2);

        if (!forced && backPressed()) {
            mode = Mode.MOVE_SELECT;
            cursor = chosenMoves[currentSlot] >= 0
                    ? chosenMoves[currentSlot]
                    : 0;
            return;
        }

        if (confirmPressed()
                && state.switchActiveMonster(slot, cursor)) {

            if (forced) {
                beginRound();
            } else {
                mode = Mode.MOVE_SELECT;
                cursor = 0;
            }
        }
    }

    /**
     * Resets the selection state for a new round.
     */
    private void beginRound() {
        Arrays.fill(chosenMoves, NO_SELECTION);
        Arrays.fill(chosenTargets, NO_SELECTION);

        mode = Mode.MOVE_SELECT;

        int first = nextActingSlot(0);
        currentSlot = Math.max(first, 0);

        cursor = state.getPhase() == BattleState.Phase.FORCED_SWITCH
                ? firstSwitchableReserve()
                : 0;

        targetCursor = 0;
    }

    /**
     * Returns to the previous acting player slot.
     */
    private void goBackOneSlot() {
        for (int slot = currentSlot - 1; slot >= 0; slot--) {
            if (state.canSlotAct(slot)) {
                chosenMoves[slot] = NO_SELECTION;
                chosenTargets[slot] = NO_SELECTION;

                currentSlot = slot;
                cursor = 0;
                mode = Mode.MOVE_SELECT;
                return;
            }
        }
    }

    /**
     * Returns the next active slot that can act.
     *
     * @param from starting slot
     * @return next valid slot, or -1
     */
    private int nextActingSlot(int from) {
        for (int slot = from; slot < BattleParty.ACTIVE_SLOTS; slot++) {
            if (state.canSlotAct(slot)) {
                return slot;
            }
        }

        return -1;
    }

    /**
     * Returns the first reserve monster that can fight.
     *
     * @return reserve index, or zero if none exists
     */
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
     * Navigates a grid with the specified number of columns.
     *
     * @param current current cursor
     * @param count number of available cells
     * @param columns number of columns
     * @return new cursor
     */
    private int navigate(int current, int count, int columns) {
        if (count <= 0) {
            return 0;
        }

        int rows = (count + columns - 1) / columns;

        int column = current % columns + horizontalInput();
        int row = current / columns + verticalInput();

        column = Math.max(0, Math.min(columns - 1, column));
        row = Math.max(0, Math.min(rows - 1, row));

        int candidate = row * columns + column;

        return candidate < count ? candidate : current;
    }

    /**
     * Navigates the fixed 3-by-2 enemy formation while skipping invalid targets.
     *
     * @param current current formation position index
     * @param validTargets positions allowed by the selected move
     * @return selected formation position index
     */
    private int navigateTargets(
            int current,
            List<EnemyFormation.Position> validTargets) {

        int column = current % EnemyFormation.SLOTS_PER_ROW;
        int row = current / EnemyFormation.SLOTS_PER_ROW;

        column = Math.max(0, Math.min(
                EnemyFormation.SLOTS_PER_ROW - 1,
                column + horizontalInput()));

        row = Math.max(0, Math.min(1, row + verticalInput()));

        int candidate = row * EnemyFormation.SLOTS_PER_ROW + column;

        int bestIndex = current;
        int bestDistance = Integer.MAX_VALUE;

        for (EnemyFormation.Position position : validTargets) {
            int index = state.getEnemyFormation().getPositionIndex(position);

            int candidateRow = index / EnemyFormation.SLOTS_PER_ROW;
            int candidateColumn = index % EnemyFormation.SLOTS_PER_ROW;

            int distance = Math.abs(candidateRow - row)
                    + Math.abs(candidateColumn - column);

            if (distance < bestDistance) {
                bestDistance = distance;
                bestIndex = index;
            }
        }

        return bestIndex;
    }

    /**
     * Reads horizontal direction input.
     *
     * @return -1 for left, 1 for right, or zero
     */
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

    /**
     * Reads vertical direction input.
     *
     * @return -1 for up, 1 for down, or zero
     */
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

    /**
     * Checks whether E or Enter was pressed.
     */
    private boolean confirmPressed() {
        return input.consumeJustPressed(KeyEvent.VK_E)
                | input.consumeJustPressed(KeyEvent.VK_ENTER);
    }

    /**
     * Checks whether X or Backspace was pressed.
     */
    private boolean backPressed() {
        return input.consumeJustPressed(KeyEvent.VK_X)
                | input.consumeJustPressed(KeyEvent.VK_BACK_SPACE);
    }

    /**
     * Checks whether Q was pressed.
     */
    private boolean switchPressed() {
        return input.consumeJustPressed(KeyEvent.VK_Q);
    }

    /** Returns the battle state. */
    public BattleState getState() {
        return state;
    }

    /**
     * Returns the current menu mode.
     *
     * @return current mode
     */
    public Mode getMode() {
        if (state.getPhase() == BattleState.Phase.FORCED_SWITCH) {
            return Mode.SWITCH_SELECT;
        }

        return mode;
    }

    /**
     * Returns the active slot being commanded.
     *
     * @return active slot index
     */
    public int getCurrentSlot() {
        return state.getPhase() == BattleState.Phase.FORCED_SWITCH
                ? state.getSlotNeedingReplacement()
                : currentSlot;
    }

    /** Returns the current menu cursor. */
    public int getCursor() {
        return cursor;
    }

    /**
     * Returns the move queued for a player slot.
     *
     * @param slot active slot
     * @return move index, or -1
     */
    public int getChosenMove(int slot) {
        return chosenMoves[slot];
    }

    /**
     * Returns the target selected for a player slot.
     *
     * @param slot active slot
     * @return formation position index, or -1
     */
    public int getChosenTarget(int slot) {
        return chosenTargets[slot];
    }

    /** Returns the target cursor's formation position index. */
    public int getSelectedTargetIndex() {
        return targetCursor;
    }

    /**
     * Returns the valid targets for the move currently being selected.
     *
     * @return valid target positions
     */
    public List<EnemyFormation.Position> getTargetablePositionsForCurrentMove() {
        if (mode != Mode.TARGET_SELECT || currentSlot < 0
                || currentSlot >= BattleParty.ACTIVE_SLOTS
                || chosenMoves[currentSlot] < 0) {
            return List.of();
        }

        BattleMonster monster =
                state.getPlayerParty().getActive(currentSlot);

        return state.getTargetablePositions(
                monster.getMove(chosenMoves[currentSlot]));
    }

    /**
     * Checks whether an earlier acting slot exists.
     *
     * @return true if the player can go back
     */
    public boolean canGoBack() {
        for (int slot = currentSlot - 1; slot >= 0; slot--) {
            if (state.canSlotAct(slot)) {
                return true;
            }
        }

        return false;
    }

    /** Returns true when the player has left the battle. */
    public boolean isFinished() {
        return finished;
    }

    /** Returns true if the player won. */
    public boolean isPlayerWon() {
        return playerWon;
    }
}