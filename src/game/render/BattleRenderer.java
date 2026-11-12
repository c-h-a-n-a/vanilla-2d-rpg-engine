package game.render;

import game.battle.BattleMonster;
import game.battle.BattleParty;
import game.battle.BattleState;
import game.entity.Monster;
import game.state.BattleController;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

/**
 * Draws the battle screen. It only reads battle state through the controller
 * and never changes battle results.
 */
public class BattleRenderer {

        /** Size of a monster sprite in its own pixels. */
        private static final int SPRITE_SOURCE_SIZE = 64;

        /** How much sprites are enlarged on screen (1 = native 64x64). */
        private static final int SPRITE_SCALE = 2;

        private static final int SPRITE_DRAW_SIZE = SPRITE_SOURCE_SIZE * SPRITE_SCALE;

        // Layout as fractions of the screen width / offsets from the ground line.
        private static final double[] PLAYER_SPRITE_X = {0.06, 0.27};
        private static final double[] PLAYER_PANEL_X = {0.03, 0.30};
        private static final int[] PLAYER_PLATFORM_OFFSET_Y = {30, 0};
        private static final double ENEMY_SPRITE_X = 0.70;
        private static final int ENEMY_PLATFORM_OFFSET_Y = 15;

        private static final int INFO_PANEL_HEIGHT = 62;
        private static final int BOTTOM_PANEL_HEIGHT = 150;

        private static final Color BACKGROUND = new Color(25, 30, 48);
        private static final Color PLATFORM = new Color(75, 110, 85);
        private static final Color PANEL = new Color(235, 235, 220);
        private static final Color TEXT = new Color(35, 40, 50);
        private static final Color DIM_TEXT = new Color(140, 140, 130);
        private static final Color SELECTED = new Color(255, 215, 90);
        private static final Color HP_TRACK = new Color(60, 60, 60);

        private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 16);
        private static final Font BODY_FONT = new Font("Arial", Font.PLAIN, 14);
        private static final Font SMALL_FONT = new Font("Arial", Font.PLAIN, 12);

        /**
         * Draws the whole battle screen.
         *
         * @param overworldMonster the encountered overworld monster (used for the enemy's colour)
         * @param controller       the battle controller, which exposes the state and menu
         */
        public void render(
                Graphics2D graphics,
                int screenWidth,
                int screenHeight,
                Monster overworldMonster,
                BattleController controller
        ) {
                BattleState state = controller.getState();
                BattleParty party = state.getPlayerParty();

                graphics.setColor(BACKGROUND);
                graphics.fillRect(0, 0, screenWidth, screenHeight);

                int groundY = screenHeight / 2 + 40;
                int panelWidth = (int) (screenWidth * 0.24);

                boolean choosing = state.getPhase() == BattleState.Phase.COMMAND_SELECTION
                        || state.getPhase() == BattleState.Phase.FORCED_SWITCH;
                int highlightedSlot = choosing ? controller.getCurrentSlot() : -1;

                drawPlayerSide(graphics, screenWidth, groundY, panelWidth,
                        controller, party, highlightedSlot);

                drawEnemySide(graphics, screenWidth, groundY, panelWidth,
                        state.getEnemyMonster(), new Color(overworldMonster.getType().getRgb()));

                drawPartyPips(graphics, (int) (screenWidth * 0.03), 98, party);

                drawBottomPanel(graphics, screenWidth, screenHeight, controller);
        }

        /** Draws both active player monsters with their platforms and info panels. */
        private void drawPlayerSide(
                Graphics2D graphics,
                int screenWidth,
                int groundY,
                int panelWidth,
                BattleController controller,
                BattleParty party,
                int highlightedSlot
        ) {
                for (int slot = 0; slot < BattleParty.ACTIVE_SLOTS; slot++) {
                        BattleMonster monster = party.getActive(slot);
                        if (monster == null) {
                                continue;
                        }

                        int spriteX = (int) (screenWidth * PLAYER_SPRITE_X[slot]);
                        int platformY = groundY + PLAYER_PLATFORM_OFFSET_Y[slot];
                        int spriteY = platformY - SPRITE_DRAW_SIZE + 30;
                        boolean highlighted = slot == highlightedSlot;

                        drawPlatform(graphics, spriteX, platformY);

                        if (!monster.isFainted()) {
                                drawPlaceholderSprite(graphics, spriteX, spriteY, true, colorFor(monster));
                        }

                        if (highlighted) {
                                drawSelectionMarker(graphics, spriteX + SPRITE_DRAW_SIZE / 2, spriteY - 6);
                        }

                        int chosen = controller.getChosenMove(slot);
                        String note = chosen >= 0 ? "Next: " + monster.getMove(chosen).getName() : "";

                        drawInfoPanel(graphics,
                                (int) (screenWidth * PLAYER_PANEL_X[slot]), 25,
                                panelWidth, monster, note, highlighted);
                }
        }

        /** Draws the enemy with its platform and info panel. */
        private void drawEnemySide(
                Graphics2D graphics,
                int screenWidth,
                int groundY,
                int panelWidth,
                BattleMonster enemy,
                Color enemyColor
        ) {
                int spriteX = (int) (screenWidth * ENEMY_SPRITE_X);
                int platformY = groundY + ENEMY_PLATFORM_OFFSET_Y;
                int spriteY = platformY - SPRITE_DRAW_SIZE + 30;

                drawPlatform(graphics, spriteX, platformY);

                if (!enemy.isFainted()) {
                        drawPlaceholderSprite(graphics, spriteX, spriteY, false, enemyColor);
                }

                drawInfoPanel(graphics,
                        screenWidth - panelWidth - (int) (screenWidth * 0.03), 25,
                        panelWidth, enemy, "Wild", false);
        }

        /** Draws an oval ground platform under a sprite. */
        private void drawPlatform(Graphics2D graphics, int spriteX, int platformY) {
                graphics.setColor(PLATFORM);
                graphics.fillOval(spriteX - 25, platformY, SPRITE_DRAW_SIZE + 50, 44);
        }

        /**
         * Draws a placeholder creature in a 64x64 box, scaled by {@link #SPRITE_SCALE}.
         * Replace the drawing code with the real sprite image when art exists; the
         * transform keeps it pixel-aligned and handles facing left.
         */
        private void drawPlaceholderSprite(
                Graphics2D graphics,
                int x,
                int y,
                boolean facingRight,
                Color color
        ) {
                AffineTransform saved = graphics.getTransform();

                graphics.translate(x, y);
                if (facingRight) {
                        graphics.scale(SPRITE_SCALE, SPRITE_SCALE);
                } else {
                        graphics.translate(SPRITE_DRAW_SIZE, 0);
                        graphics.scale(-SPRITE_SCALE, SPRITE_SCALE);
                }

                graphics.setColor(color);
                graphics.fillOval(6, 26, 40, 30);   // body
                graphics.fillOval(30, 12, 26, 26);  // head
                graphics.fillOval(10, 50, 14, 10);  // back foot
                graphics.fillOval(30, 50, 14, 10);  // front foot

                graphics.setColor(Color.WHITE);
                graphics.fillOval(44, 20, 6, 6);    // eye

                graphics.setColor(Color.BLACK);
                graphics.fillOval(47, 22, 3, 3);    // pupil

                graphics.setTransform(saved);
        }

        /** Draws a small downward arrow above the monster currently being commanded. */
        private void drawSelectionMarker(Graphics2D graphics, int centerX, int tipY) {
                graphics.setColor(SELECTED);
                graphics.fillPolygon(
                        new int[]{centerX - 9, centerX + 9, centerX},
                        new int[]{tipY - 14, tipY - 14, tipY},
                        3);
        }

        /** Draws a name plate with HP bar, HP numbers and an optional note on the right. */
        private void drawInfoPanel(
                Graphics2D graphics,
                int x,
                int y,
                int width,
                BattleMonster monster,
                String note,
                boolean highlighted
        ) {
                graphics.setColor(PANEL);
                graphics.fillRoundRect(x, y, width, INFO_PANEL_HEIGHT, 15, 15);

                if (highlighted) {
                        Stroke saved = graphics.getStroke();
                        graphics.setStroke(new BasicStroke(3));
                        graphics.setColor(SELECTED);
                        graphics.drawRoundRect(x, y, width, INFO_PANEL_HEIGHT, 15, 15);
                        graphics.setStroke(saved);
                }

                graphics.setColor(TEXT);
                graphics.setFont(TITLE_FONT);
                graphics.drawString(monster.getName(), x + 12, y + 22);

                int barX = x + 12;
                int barY = y + 31;
                int barWidth = width - 24;
                double ratio = (double) monster.getCurrentHp() / monster.getMaxHp();

                graphics.setColor(HP_TRACK);
                graphics.fillRect(barX, barY, barWidth, 8);

                graphics.setColor(ratio > 0.5 ? new Color(80, 190, 90)
                        : ratio > 0.2 ? new Color(230, 190, 50)
                        : new Color(210, 70, 60));
                graphics.fillRect(barX, barY, (int) (barWidth * ratio), 8);

                graphics.setColor(TEXT);
                graphics.setFont(SMALL_FONT);
                graphics.drawString(
                        "HP " + monster.getCurrentHp() + " / " + monster.getMaxHp(),
                        barX, y + 54);

                if (!note.isEmpty()) {
                        int noteWidth = graphics.getFontMetrics().stringWidth(note);
                        graphics.drawString(note, x + width - 12 - noteWidth, y + 54);
                }
        }

        /** Draws six pips showing the party: green = alive, red = fainted, outline = empty slot. */
        private void drawPartyPips(Graphics2D graphics, int x, int y, BattleParty party) {
                var members = party.getAllMembers();

                for (int i = 0; i < BattleParty.MAX_SIZE; i++) {
                        int pipX = x + i * 20;

                        if (i < members.size()) {
                                graphics.setColor(members.get(i).isFainted()
                                        ? new Color(210, 70, 60)
                                        : new Color(80, 190, 90));
                                graphics.fillOval(pipX, y, 14, 14);
                        } else {
                                graphics.setColor(DIM_TEXT);
                                graphics.drawOval(pipX, y, 14, 14);
                        }
                }
        }

        /** Draws the bottom panel: messages on the left, menu or prompt on the right. */
        private void drawBottomPanel(
                Graphics2D graphics,
                int screenWidth,
                int screenHeight,
                BattleController controller
        ) {
                BattleState state = controller.getState();
                BattleParty party = state.getPlayerParty();

                int panelY = screenHeight - BOTTOM_PANEL_HEIGHT - 15;

                graphics.setColor(PANEL);
                graphics.fillRoundRect(25, panelY, screenWidth - 50, BOTTOM_PANEL_HEIGHT, 20, 20);

                // Messages, one line per '\n'.
                graphics.setColor(TEXT);
                graphics.setFont(BODY_FONT);
                String[] lines = state.getLastMessage().split("\n");
                for (int i = 0; i < lines.length; i++) {
                        graphics.drawString(lines[i], 50, panelY + 32 + i * 20);
                }

                int menuX = screenWidth / 2 + 20;
                int columnWidth = (screenWidth / 2 - 70) / 2;
                int gridTop = panelY + 44;

                BattleState.Phase phase = state.getPhase();

                if (phase == BattleState.Phase.VICTORY || phase == BattleState.Phase.DEFEAT) {
                        graphics.setFont(TITLE_FONT);
                        graphics.drawString("Press E to continue", menuX, panelY + 70);
                        return;
                }

                BattleMonster current = party.getActive(controller.getCurrentSlot());
                String title;
                String hint;

                if (phase == BattleState.Phase.FORCED_SWITCH) {
                        title = "Send out who?";
                        hint = "E: confirm";
                } else if (controller.getMode() == BattleController.Mode.SWITCH_SELECT) {
                        title = "Swap out " + current.getName() + " for:";
                        hint = "E: confirm   X: back";
                } else {
                        title = "What will " + current.getName() + " do?";
                        hint = "E: confirm"
                                + (party.hasLivingReserve() ? "   Q: switch" : "")
                                + (controller.canGoBack() ? "   X: back" : "");
                }

                graphics.setColor(TEXT);
                graphics.setFont(TITLE_FONT);
                graphics.drawString(title, menuX, panelY + 28);

                if (controller.getMode() == BattleController.Mode.SWITCH_SELECT) {
                        drawReserveGrid(graphics, party, menuX, gridTop, columnWidth, controller.getCursor());
                } else {
                        drawMoveGrid(graphics, current, menuX, gridTop, columnWidth, controller.getCursor());
                }

                graphics.setColor(DIM_TEXT);
                graphics.setFont(SMALL_FONT);
                graphics.drawString(hint, menuX, panelY + BOTTOM_PANEL_HEIGHT - 10);
        }

        /** Draws the 2x2 grid of a monster's four moves with their power. */
        private void drawMoveGrid(
                Graphics2D graphics,
                BattleMonster monster,
                int menuX,
                int gridTop,
                int columnWidth,
                int cursor
        ) {
                for (int i = 0; i < BattleMonster.MOVE_COUNT; i++) {
                        int x = menuX + (i % 2) * columnWidth;
                        int top = gridTop + (i / 2) * 44;

                        drawGridCell(graphics, x, top, columnWidth, i == cursor);

                        graphics.setColor(TEXT);
                        graphics.setFont(TITLE_FONT);
                        graphics.drawString(monster.getMove(i).getName(), x, top + 26);

                        graphics.setColor(DIM_TEXT);
                        graphics.setFont(SMALL_FONT);
                        String power = "PWR " + monster.getMove(i).getPower();
                        int powerWidth = graphics.getFontMetrics().stringWidth(power);
                        graphics.drawString(power, x + columnWidth - 20 - powerWidth, top + 26);
                }
        }

        /** Draws the 2x2 grid of reserve monsters with their HP; fainted ones are dimmed. */
        private void drawReserveGrid(
                Graphics2D graphics,
                BattleParty party,
                int menuX,
                int gridTop,
                int columnWidth,
                int cursor
        ) {
                for (int i = 0; i < party.getReserveCount(); i++) {
                        BattleMonster member = party.getReserveMember(i);

                        int x = menuX + (i % 2) * columnWidth;
                        int top = gridTop + (i / 2) * 44;

                        drawGridCell(graphics, x, top, columnWidth, i == cursor);

                        graphics.setColor(member.isFainted() ? DIM_TEXT : TEXT);
                        graphics.setFont(TITLE_FONT);
                        graphics.drawString(member.getName(), x, top + 17);

                        graphics.setFont(SMALL_FONT);
                        graphics.drawString(
                                member.isFainted()
                                        ? "Fainted"
                                        : "HP " + member.getCurrentHp() + " / " + member.getMaxHp(),
                                x, top + 33);
                }
        }

        /** Draws the highlight behind a grid cell when it is selected. */
        private void drawGridCell(
                Graphics2D graphics,
                int x,
                int top,
                int columnWidth,
                boolean selected
        ) {
                if (selected) {
                        graphics.setColor(SELECTED);
                        graphics.fillRoundRect(x - 10, top, columnWidth - 10, 40, 10, 10);
                }
        }

        /**
         * Picks a stable placeholder colour from the monster's name.
         * Remove this once monsters have real sprites.
         */
        private Color colorFor(BattleMonster monster) {
                float hue = Math.floorMod(monster.getName().hashCode(), 360) / 360f;
                return Color.getHSBColor(hue, 0.55f, 0.85f);
        }
}