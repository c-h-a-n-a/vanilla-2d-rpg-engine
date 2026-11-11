package game.render;

import game.battle.BattleMonster;
import game.battle.BattleState;
import game.entity.Monster;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * Draws the battle screen. It only reads {@link BattleState}; it never
 * changes battle results.
 */
public class BattleRenderer {

    private static final Color BACKGROUND = new Color(25, 30, 48);
    private static final Color PANEL = new Color(235, 235, 220);
    private static final Color TEXT = new Color(35, 40, 50);
    private static final Color SELECTED = new Color(255, 215, 90);

    /**
     * Draws the whole battle screen.
     *
     * @param monster           the overworld monster (used for its colour)
     * @param state             the current battle
     * @param selectedMoveIndex move slot highlighted in the menu
     */
    public void render(
            Graphics2D graphics,
            int screenWidth,
            int screenHeight,
            Monster monster,
            BattleState state,
            int selectedMoveIndex
    ) {
        graphics.setColor(BACKGROUND);
        graphics.fillRect(0, 0, screenWidth, screenHeight);

        int groundY = screenHeight / 2 + 40;

        // Platforms on a shared ground level.
        graphics.setColor(new Color(75, 110, 85));
        graphics.fillOval(65, groundY, 280, 65);
        graphics.fillOval(screenWidth - 345, groundY, 280, 65);

        drawCreature(graphics, 150, groundY - 85, new Color(75, 145, 220));
        drawCreature(graphics, screenWidth - 250, groundY - 85,
                new Color(monster.getType().getRgb()));

        drawInfoPanel(graphics, 35, 35, 240, 75, state.getEnemyMonster());
        drawInfoPanel(graphics, screenWidth - 275, groundY + 90, 240, 75,
                state.getPlayerMonster());

        drawBottomPanel(graphics, screenWidth, screenHeight, state, selectedMoveIndex);
    }

    /** Draws the message box (left) and the move menu or continue prompt (right). */
    private void drawBottomPanel(
            Graphics2D graphics,
            int screenWidth,
            int screenHeight,
            BattleState state,
            int selectedMoveIndex
    ) {
        int panelHeight = 140;
        int panelY = screenHeight - panelHeight - 20;

        graphics.setColor(PANEL);
        graphics.fillRoundRect(25, panelY, screenWidth - 50, panelHeight, 20, 20);

        // Round messages, one line per '\n'.
        graphics.setColor(TEXT);
        graphics.setFont(new Font("Arial", Font.PLAIN, 16));

        String[] lines = state.getLastMessage().split("\n");
        for (int i = 0; i < lines.length; i++) {
            graphics.drawString(lines[i], 50, panelY + 35 + i * 24);
        }

        int menuX = screenWidth / 2 + 20;

        if (state.getPhase() != BattleState.Phase.PLAYER_TURN) {
            graphics.setFont(new Font("Arial", Font.BOLD, 16));
            graphics.drawString("Press E to continue", menuX, panelY + 70);
            return;
        }

        drawMoveMenu(graphics, state.getPlayerMonster(), menuX, panelY, screenWidth,
                selectedMoveIndex);
    }

    /** Draws the 2x2 grid of the player's four moves, highlighting the selected one. */
    private void drawMoveMenu(
            Graphics2D graphics,
            BattleMonster monster,
            int menuX,
            int panelY,
            int screenWidth,
            int selectedMoveIndex
    ) {
        int columnWidth = (screenWidth / 2 - 70) / 2;

        for (int i = 0; i < BattleMonster.MOVE_COUNT; i++) {
            int x = menuX + (i % 2) * columnWidth;
            int y = panelY + 45 + (i / 2) * 45;

            if (i == selectedMoveIndex) {
                graphics.setColor(SELECTED);
                graphics.fillRoundRect(x - 10, y - 24, columnWidth - 10, 34, 10, 10);
            }

            graphics.setColor(TEXT);
            graphics.setFont(new Font("Arial", Font.BOLD, 16));
            graphics.drawString(monster.getMove(i).getName(), x, y);
        }
    }

    /** Draws a name plate with an HP bar and HP numbers for one monster. */
    private void drawInfoPanel(
            Graphics2D graphics,
            int x, int y, int width, int height,
            BattleMonster monster
    ) {
        graphics.setColor(PANEL);
        graphics.fillRoundRect(x, y, width, height, 15, 15);

        graphics.setColor(TEXT);
        graphics.setFont(new Font("Arial", Font.BOLD, 18));
        graphics.drawString(monster.getName(), x + 15, y + 28);

        int barX = x + 15;
        int barY = y + 38;
        int barWidth = width - 30;
        double ratio = (double) monster.getCurrentHp() / monster.getMaxHp();

        graphics.setColor(new Color(60, 60, 60));
        graphics.fillRect(barX, barY, barWidth, 10);

        graphics.setColor(ratio > 0.5 ? new Color(80, 190, 90)
                : ratio > 0.2 ? new Color(230, 190, 50)
                : new Color(210, 70, 60));
        graphics.fillRect(barX, barY, (int) (barWidth * ratio), 10);

        graphics.setColor(TEXT);
        graphics.setFont(new Font("Arial", Font.PLAIN, 13));
        graphics.drawString(
                "HP " + monster.getCurrentHp() + " / " + monster.getMaxHp(),
                barX, y + 66);
    }

    /** Draws a placeholder side-view creature from simple shapes. */
    private void drawCreature(Graphics2D graphics, int x, int y, Color color) {
        graphics.setColor(color);

        // Body.
        graphics.fillOval(x, y, 105, 85);

        // Head.
        graphics.fillOval(x + 65, y - 25, 65, 65);

        // Eye.
        graphics.setColor(Color.WHITE);
        graphics.fillOval(x + 105, y - 5, 12, 12);

        graphics.setColor(Color.BLACK);
        graphics.fillOval(x + 110, y - 2, 5, 5);

        // Feet.
        graphics.setColor(color);
        graphics.fillOval(x + 15, y + 65, 35, 25);
        graphics.fillOval(x + 70, y + 65, 35, 25);
    }
}