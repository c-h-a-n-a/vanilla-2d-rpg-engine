package game.render;

import game.battle.BattleMonster;
import game.battle.BattleParty;
import game.battle.BattleState;
import game.battle.EnemyFormation;
import game.state.BattleController;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.util.List;

/**
 * Draws the battle screen.
 * It only reads battle state and never changes battle results.
 */
public class BattleRenderer {

        /** Source sprite size. */
        private static final int SPRITE_SOURCE_SIZE = 64;

        /** Player sprite scale. */
        private static final int PLAYER_SPRITE_SCALE = 2;

        /** Enemy sprite scale. */
        private static final int ENEMY_SPRITE_SCALE = 1;

        private static final int PLAYER_DRAW_SIZE =
                SPRITE_SOURCE_SIZE * PLAYER_SPRITE_SCALE;

        private static final int ENEMY_DRAW_SIZE =
                SPRITE_SOURCE_SIZE * ENEMY_SPRITE_SCALE;

        private static final double[] PLAYER_SPRITE_X = {0.06, 0.27};
        private static final double[] PLAYER_PANEL_X = {0.03, 0.30};

        private static final int[] PLAYER_PLATFORM_OFFSET_Y = {30, 0};

        private static final double[] ENEMY_SPRITE_X = {0.57, 0.71, 0.85};

        private static final int INFO_PANEL_HEIGHT = 62;
        private static final int ENEMY_INFO_PANEL_WIDTH = 112;
        private static final int ENEMY_INFO_PANEL_HEIGHT = 50;

        private static final int BOTTOM_PANEL_HEIGHT = 150;

        private static final Color BACKGROUND = new Color(25, 30, 48);
        private static final Color PLATFORM = new Color(75, 110, 85);
        private static final Color PANEL = new Color(235, 235, 220);
        private static final Color TEXT = new Color(35, 40, 50);
        private static final Color DIM_TEXT = new Color(140, 140, 130);
        private static final Color SELECTED = new Color(255, 215, 90);
        private static final Color HP_TRACK = new Color(60, 60, 60);

        private static final Font TITLE_FONT =
                new Font("Arial", Font.BOLD, 16);

        private static final Font BODY_FONT =
                new Font("Arial", Font.PLAIN, 14);

        private static final Font SMALL_FONT =
                new Font("Arial", Font.PLAIN, 12);

        /**
         * Draws the complete battle screen.
         *
         * @param graphics graphics context
         * @param screenWidth screen width
         * @param screenHeight screen height
         * @param controller battle controller
         */
        public void render(
                Graphics2D graphics,
                int screenWidth,
                int screenHeight,
                BattleController controller) {

                BattleState state = controller.getState();
                BattleParty party = state.getPlayerParty();

                graphics.setColor(BACKGROUND);
                graphics.fillRect(0, 0, screenWidth, screenHeight);

                int groundY = screenHeight / 2 + 40;
                int playerPanelWidth = (int) (screenWidth * 0.24);

                boolean choosing =
                        state.getPhase() == BattleState.Phase.COMMAND_SELECTION
                                || state.getPhase() == BattleState.Phase.FORCED_SWITCH;

                int highlightedSlot =
                        choosing ? controller.getCurrentSlot() : -1;

                drawPlayerSide(
                        graphics,
                        screenWidth,
                        groundY,
                        playerPanelWidth,
                        controller,
                        party,
                        highlightedSlot
                );

                drawEnemyFormation(
                        graphics,
                        screenWidth,
                        groundY,
                        state.getEnemyFormation(),
                        controller
                );

                drawPartyPips(
                        graphics,
                        (int) (screenWidth * 0.03),
                        98,
                        party
                );

                drawBottomPanel(
                        graphics,
                        screenWidth,
                        screenHeight,
                        controller
                );
        }

        /**
         * Draws the two active player monsters.
         *
         * @param graphics graphics context
         * @param screenWidth screen width
         * @param groundY ground position
         * @param panelWidth player information panel width
         * @param controller battle controller
         * @param party player party
         * @param highlightedSlot selected player slot
         */
        private void drawPlayerSide(
                Graphics2D graphics,
                int screenWidth,
                int groundY,
                int panelWidth,
                BattleController controller,
                BattleParty party,
                int highlightedSlot) {

                for (int slot = 0; slot < BattleParty.ACTIVE_SLOTS; slot++) {
                        BattleMonster monster = party.getActive(slot);

                        if (monster == null) {
                                continue;
                        }

                        int spriteX = (int) (screenWidth * PLAYER_SPRITE_X[slot]);

                        int platformY =
                                groundY + PLAYER_PLATFORM_OFFSET_Y[slot];

                        int spriteY =
                                platformY - PLAYER_DRAW_SIZE + 30;

                        boolean highlighted = slot == highlightedSlot;

                        drawPlatform(
                                graphics,
                                spriteX,
                                platformY,
                                PLAYER_DRAW_SIZE
                        );

                        if (!monster.isFainted()) {
                                drawPlaceholderSprite(
                                        graphics,
                                        spriteX,
                                        spriteY,
                                        true,
                                        colorFor(monster),
                                        PLAYER_SPRITE_SCALE
                                );
                        }

                        if (highlighted) {
                                drawSelectionMarker(
                                        graphics,
                                        spriteX + PLAYER_DRAW_SIZE / 2,
                                        spriteY - 6
                                );
                        }

                        int chosen = controller.getChosenMove(slot);

                        String note = chosen >= 0
                                ? "Next: " + monster.getMove(chosen).getName()
                                : "";

                        drawPlayerInfoPanel(
                                graphics,
                                (int) (screenWidth * PLAYER_PANEL_X[slot]),
                                25,
                                panelWidth,
                                monster,
                                note,
                                highlighted
                        );
                }
        }

        /**
         * Draws every occupied enemy position in the formation.
         *
         * @param graphics graphics context
         * @param screenWidth screen width
         * @param groundY ground position
         * @param formation enemy formation
         * @param controller battle controller
         */
        private void drawEnemyFormation(
                Graphics2D graphics,
                int screenWidth,
                int groundY,
                EnemyFormation formation,
                BattleController controller) {

                List<EnemyFormation.Position> validTargets =
                        controller.getTargetablePositionsForCurrentMove();

                boolean selectingTarget =
                        controller.getMode() == BattleController.Mode.TARGET_SELECT;

                int selectedIndex =
                        selectingTarget ? controller.getSelectedTargetIndex() : -1;

                for (EnemyFormation.Row row : EnemyFormation.Row.values()) {
                        for (int slot = 0; slot < EnemyFormation.SLOTS_PER_ROW; slot++) {

                                BattleMonster enemy = formation.getEnemy(row, slot);

                                if (enemy == null) {
                                        continue;
                                }

                                EnemyFormation.Position position =
                                        new EnemyFormation.Position(row, slot);

                                int positionIndex =
                                        formation.getPositionIndex(position);

                                int spriteX =
                                        (int) (screenWidth * ENEMY_SPRITE_X[slot]);

                                int platformY = row == EnemyFormation.Row.FRONT
                                        ? groundY + 10
                                        : groundY - 125;

                                int spriteY =
                                        platformY - ENEMY_DRAW_SIZE + 10;

                                boolean selected =
                                        positionIndex == selectedIndex;

                                drawPlatform(
                                        graphics,
                                        spriteX,
                                        platformY,
                                        ENEMY_DRAW_SIZE
                                );

                                if (!enemy.isFainted()) {
                                        drawPlaceholderSprite(
                                                graphics,
                                                spriteX,
                                                spriteY,
                                                false,
                                                colorFor(enemy),
                                                ENEMY_SPRITE_SCALE
                                        );
                                }

                                if (selected) {
                                        drawSelectionMarker(
                                                graphics,
                                                spriteX + ENEMY_DRAW_SIZE / 2,
                                                spriteY - 6
                                        );
                                }

                                boolean targetable = validTargets.contains(position);

                                int panelY = row == EnemyFormation.Row.FRONT
                                        ? groundY - 105
                                        : 20;

                                drawEnemyInfoPanel(
                                        graphics,
                                        spriteX + ENEMY_DRAW_SIZE / 2
                                                - ENEMY_INFO_PANEL_WIDTH / 2,
                                        panelY,
                                        enemy,
                                        selected,
                                        selectingTarget && !targetable
                                );
                        }
                }
        }

        /**
         * Draws a platform beneath a monster.
         *
         * @param graphics graphics context
         * @param spriteX sprite horizontal position
         * @param platformY platform vertical position
         * @param spriteSize sprite size
         */
        private void drawPlatform(
                Graphics2D graphics,
                int spriteX,
                int platformY,
                int spriteSize) {

                graphics.setColor(PLATFORM);

                graphics.fillOval(
                        spriteX - 20,
                        platformY,
                        spriteSize + 40,
                        34
                );
        }

        /**
         * Draws a placeholder monster sprite.
         *
         * @param graphics graphics context
         * @param x horizontal position
         * @param y vertical position
         * @param facingRight true if the sprite faces right
         * @param color monster color
         * @param scale sprite scale
         */
        private void drawPlaceholderSprite(
                Graphics2D graphics,
                int x,
                int y,
                boolean facingRight,
                Color color,
                int scale) {

                int drawSize = SPRITE_SOURCE_SIZE * scale;

                AffineTransform saved = graphics.getTransform();

                graphics.translate(x, y);

                if (facingRight) {
                        graphics.scale(scale, scale);
                } else {
                        graphics.translate(drawSize, 0);
                        graphics.scale(-scale, scale);
                }

                graphics.setColor(color);
                graphics.fillOval(6, 26, 40, 30);
                graphics.fillOval(30, 12, 26, 26);
                graphics.fillOval(10, 50, 14, 10);
                graphics.fillOval(30, 50, 14, 10);

                graphics.setColor(Color.WHITE);
                graphics.fillOval(44, 20, 6, 6);

                graphics.setColor(Color.BLACK);
                graphics.fillOval(47, 22, 3, 3);

                graphics.setTransform(saved);
        }

        /**
         * Draws a downward selection marker.
         *
         * @param graphics graphics context
         * @param centerX marker center
         * @param tipY marker tip position
         */
        private void drawSelectionMarker(
                Graphics2D graphics,
                int centerX,
                int tipY) {

                graphics.setColor(SELECTED);

                graphics.fillPolygon(
                        new int[] {
                                centerX - 9,
                                centerX + 9,
                                centerX
                        },
                        new int[] {
                                tipY - 14,
                                tipY - 14,
                                tipY
                        },
                        3
                );
        }

        /**
         * Draws a player information panel.
         *
         * @param graphics graphics context
         * @param x horizontal position
         * @param y vertical position
         * @param width panel width
         * @param monster monster to display
         * @param note optional note
         * @param highlighted true if selected
         */
        private void drawPlayerInfoPanel(
                Graphics2D graphics,
                int x,
                int y,
                int width,
                BattleMonster monster,
                String note,
                boolean highlighted) {

                graphics.setColor(PANEL);
                graphics.fillRoundRect(
                        x,
                        y,
                        width,
                        INFO_PANEL_HEIGHT,
                        15,
                        15
                );

                if (highlighted) {
                        drawPanelBorder(
                                graphics,
                                x,
                                y,
                                width,
                                INFO_PANEL_HEIGHT
                        );
                }

                graphics.setColor(TEXT);
                graphics.setFont(TITLE_FONT);
                graphics.drawString(monster.getName(), x + 12, y + 22);

                drawHpBar(
                        graphics,
                        x + 12,
                        y + 31,
                        width - 24,
                        monster
                );

                graphics.setColor(TEXT);
                graphics.setFont(SMALL_FONT);

                graphics.drawString(
                        "HP " + monster.getCurrentHp()
                                + " / " + monster.getMaxHp(),
                        x + 12,
                        y + 54
                );

                if (!note.isEmpty()) {
                        int noteWidth =
                                graphics.getFontMetrics().stringWidth(note);

                        graphics.drawString(
                                note,
                                x + width - 12 - noteWidth,
                                y + 54
                        );
                }
        }

        /**
         * Draws a compact enemy information panel.
         *
         * @param graphics graphics context
         * @param x horizontal position
         * @param y vertical position
         * @param monster enemy monster
         * @param selected true if this enemy is selected
         * @param dimmed true if this enemy cannot be targeted by the current move
         */
        private void drawEnemyInfoPanel(
                Graphics2D graphics,
                int x,
                int y,
                BattleMonster monster,
                boolean selected,
                boolean dimmed) {

                graphics.setColor(dimmed
                        ? new Color(190, 190, 180)
                        : PANEL);

                graphics.fillRoundRect(
                        x,
                        y,
                        ENEMY_INFO_PANEL_WIDTH,
                        ENEMY_INFO_PANEL_HEIGHT,
                        12,
                        12
                );

                if (selected) {
                        drawPanelBorder(
                                graphics,
                                x,
                                y,
                                ENEMY_INFO_PANEL_WIDTH,
                                ENEMY_INFO_PANEL_HEIGHT
                        );
                }

                graphics.setColor(monster.isFainted() ? DIM_TEXT : TEXT);
                graphics.setFont(TITLE_FONT);

                graphics.drawString(
                        monster.getName(),
                        x + 8,
                        y + 17
                );

                drawHpBar(
                        graphics,
                        x + 8,
                        y + 24,
                        ENEMY_INFO_PANEL_WIDTH - 16,
                        monster
                );

                graphics.setColor(TEXT);
                graphics.setFont(SMALL_FONT);

                graphics.drawString(
                        monster.isFainted()
                                ? "FAINTED"
                                : "HP " + monster.getCurrentHp()
                                + " / " + monster.getMaxHp(),
                        x + 8,
                        y + 43
                );
        }

        /**
         * Draws an HP bar.
         *
         * @param graphics graphics context
         * @param x horizontal position
         * @param y vertical position
         * @param width bar width
         * @param monster monster whose HP is displayed
         */
        private void drawHpBar(
                Graphics2D graphics,
                int x,
                int y,
                int width,
                BattleMonster monster) {

                double ratio = (double) monster.getCurrentHp()
                        / monster.getMaxHp();

                graphics.setColor(HP_TRACK);
                graphics.fillRect(x, y, width, 8);

                graphics.setColor(
                        ratio > 0.5
                                ? new Color(80, 190, 90)
                                : ratio > 0.2
                                ? new Color(230, 190, 50)
                                : new Color(210, 70, 60)
                );

                graphics.fillRect(
                        x,
                        y,
                        (int) (width * ratio),
                        8
                );
        }

        /**
         * Draws a highlighted panel border.
         *
         * @param graphics graphics context
         * @param x horizontal position
         * @param y vertical position
         * @param width panel width
         * @param height panel height
         */
        private void drawPanelBorder(
                Graphics2D graphics,
                int x,
                int y,
                int width,
                int height) {

                Stroke saved = graphics.getStroke();

                graphics.setStroke(new BasicStroke(3));
                graphics.setColor(SELECTED);

                graphics.drawRoundRect(
                        x,
                        y,
                        width,
                        height,
                        15,
                        15
                );

                graphics.setStroke(saved);
        }

        /**
         * Draws six pips representing the player's party.
         *
         * @param graphics graphics context
         * @param x horizontal starting position
         * @param y vertical position
         * @param party player party
         */
        private void drawPartyPips(
                Graphics2D graphics,
                int x,
                int y,
                BattleParty party) {

                var members = party.getAllMembers();

                for (int i = 0; i < BattleParty.MAX_SIZE; i++) {
                        int pipX = x + i * 20;

                        if (i < members.size()) {
                                graphics.setColor(
                                        members.get(i).isFainted()
                                                ? new Color(210, 70, 60)
                                                : new Color(80, 190, 90)
                                );

                                graphics.fillOval(pipX, y, 14, 14);
                        } else {
                                graphics.setColor(DIM_TEXT);
                                graphics.drawOval(pipX, y, 14, 14);
                        }
                }
        }

        /**
         * Draws the bottom panel and its current menu.
         *
         * @param graphics graphics context
         * @param screenWidth screen width
         * @param screenHeight screen height
         * @param controller battle controller
         */
        private void drawBottomPanel(
                Graphics2D graphics,
                int screenWidth,
                int screenHeight,
                BattleController controller) {

                BattleState state = controller.getState();
                BattleParty party = state.getPlayerParty();

                int panelY = screenHeight - BOTTOM_PANEL_HEIGHT - 15;

                graphics.setColor(PANEL);

                graphics.fillRoundRect(
                        25,
                        panelY,
                        screenWidth - 50,
                        BOTTOM_PANEL_HEIGHT,
                        20,
                        20
                );

                drawMessages(graphics, state, panelY);

                int menuX = screenWidth / 2 + 20;
                int columnWidth = (screenWidth / 2 - 70) / 2;
                int gridTop = panelY + 44;

                BattleState.Phase phase = state.getPhase();

                if (phase == BattleState.Phase.VICTORY
                        || phase == BattleState.Phase.DEFEAT) {

                        graphics.setColor(TEXT);
                        graphics.setFont(TITLE_FONT);
                        graphics.drawString(
                                "Press E to continue",
                                menuX,
                                panelY + 70
                        );

                        return;
                }

                if (controller.getMode() == BattleController.Mode.TARGET_SELECT) {
                        drawTargetSelection(
                                graphics,
                                screenWidth,
                                panelY,
                                controller,
                                menuX,
                                gridTop
                        );

                        return;
                }

                int currentSlot = controller.getCurrentSlot();

                if (currentSlot < 0 || currentSlot >= BattleParty.ACTIVE_SLOTS) {
                        return;
                }

                BattleMonster current = party.getActive(currentSlot);

                if (current == null) {
                        return;
                }

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
                        hint = "E: choose target"
                                + (party.hasLivingReserve() ? "   Q: switch" : "")
                                + (controller.canGoBack() ? "   X: back" : "");
                }

                graphics.setColor(TEXT);
                graphics.setFont(TITLE_FONT);
                graphics.drawString(title, menuX, panelY + 28);

                if (controller.getMode() == BattleController.Mode.SWITCH_SELECT) {
                        drawReserveGrid(
                                graphics,
                                party,
                                menuX,
                                gridTop,
                                columnWidth,
                                controller.getCursor()
                        );
                } else {
                        drawMoveGrid(
                                graphics,
                                current,
                                menuX,
                                gridTop,
                                columnWidth,
                                controller.getCursor()
                        );
                }

                graphics.setColor(DIM_TEXT);
                graphics.setFont(SMALL_FONT);
                graphics.drawString(
                        hint,
                        menuX,
                        panelY + BOTTOM_PANEL_HEIGHT - 10
                );
        }

        /**
         * Draws the latest battle messages.
         *
         * @param graphics graphics context
         * @param state battle state
         * @param panelY bottom panel vertical position
         */
        private void drawMessages(
                Graphics2D graphics,
                BattleState state,
                int panelY) {

                graphics.setColor(TEXT);
                graphics.setFont(BODY_FONT);

                String[] lines = state.getLastMessage().split("\n");

                int maxLines = 5;

                for (int i = 0; i < Math.min(lines.length, maxLines); i++) {
                        graphics.drawString(
                                lines[i],
                                50,
                                panelY + 32 + i * 20
                        );
                }
        }

        /**
         * Draws the enemy target-selection grid.
         *
         * @param graphics graphics context
         * @param screenWidth screen width
         * @param panelY bottom panel vertical position
         * @param controller battle controller
         * @param menuX grid horizontal starting position
         * @param gridTop grid vertical starting position
         */
        private void drawTargetSelection(
                Graphics2D graphics,
                int screenWidth,
                int panelY,
                BattleController controller,
                int menuX,
                int gridTop) {

                int currentSlot = controller.getCurrentSlot();

                BattleMonster attacker =
                        controller.getState()
                                .getPlayerParty()
                                .getActive(currentSlot);

                BattleMoveInfo moveInfo =
                        getSelectedMoveInfo(attacker,
                                controller.getChosenMove(currentSlot));

                graphics.setColor(TEXT);
                graphics.setFont(TITLE_FONT);

                graphics.drawString(
                        "Choose target for " + attacker.getName(),
                        menuX,
                        panelY + 28
                );

                graphics.setFont(SMALL_FONT);
                graphics.setColor(DIM_TEXT);

                graphics.drawString(
                        moveInfo.description,
                        menuX,
                        panelY + 43
                );

                int columnWidth = (screenWidth / 2 - 70) / 3;

                List<EnemyFormation.Position> validTargets =
                        controller.getTargetablePositionsForCurrentMove();

                EnemyFormation formation =
                        controller.getState().getEnemyFormation();

                for (int index = 0; index < 6; index++) {
                        EnemyFormation.Position position =
                                formation.getPositionByIndex(index);

                        BattleMonster enemy = formation.getEnemy(
                                position.row(),
                                position.slot()
                        );

                        int x = menuX
                                + (index % 3) * columnWidth;

                        int y = gridTop
                                + (index / 3) * 44;

                        boolean selected =
                                index == controller.getSelectedTargetIndex();

                        boolean targetable =
                                validTargets.contains(position);

                        drawGridCell(
                                graphics,
                                x,
                                y,
                                columnWidth,
                                selected
                        );

                        graphics.setFont(SMALL_FONT);

                        if (enemy == null) {
                                graphics.setColor(DIM_TEXT);
                                graphics.drawString("Empty", x, y + 25);
                        } else {
                                graphics.setColor(
                                        enemy.isFainted() || !targetable
                                                ? DIM_TEXT
                                                : TEXT
                                );

                                graphics.drawString(
                                        enemy.getName(),
                                        x,
                                        y + 25
                                );
                        }
                }

                graphics.setColor(DIM_TEXT);
                graphics.setFont(SMALL_FONT);

                graphics.drawString(
                        "E: confirm   X: back",
                        menuX,
                        panelY + BOTTOM_PANEL_HEIGHT - 10
                );
        }

        /**
         * Holds the selected move description for the target menu.
         */
        private record BattleMoveInfo(String description) {
        }

        /**
         * Creates the target menu description for the selected move.
         *
         * @param attacker attacking monster
         * @param moveIndex selected move index
         * @return move description
         */
        private BattleMoveInfo getSelectedMoveInfo(
                BattleMonster attacker,
                int moveIndex) {

                if (attacker == null || moveIndex < 0) {
                        return new BattleMoveInfo("");
                }

                String type = attacker.getMove(moveIndex).getTargetType().name();

                return new BattleMoveInfo(
                        attacker.getMove(moveIndex).getName() + " - " + type
                );
        }

        /**
         * Draws the four-move grid.
         *
         * @param graphics graphics context
         * @param monster current monster
         * @param menuX horizontal starting position
         * @param gridTop vertical starting position
         * @param columnWidth column width
         * @param cursor selected cell
         */
        private void drawMoveGrid(
                Graphics2D graphics,
                BattleMonster monster,
                int menuX,
                int gridTop,
                int columnWidth,
                int cursor) {

                for (int i = 0; i < BattleMonster.MOVE_COUNT; i++) {
                        int x = menuX + (i % 2) * columnWidth;
                        int top = gridTop + (i / 2) * 44;

                        drawGridCell(
                                graphics,
                                x,
                                top,
                                columnWidth,
                                i == cursor
                        );

                        graphics.setColor(TEXT);
                        graphics.setFont(TITLE_FONT);

                        graphics.drawString(
                                monster.getMove(i).getName(),
                                x,
                                top + 26
                        );

                        graphics.setColor(DIM_TEXT);
                        graphics.setFont(SMALL_FONT);

                        String power =
                                "PWR " + monster.getMove(i).getPower();

                        int powerWidth =
                                graphics.getFontMetrics().stringWidth(power);

                        graphics.drawString(
                                power,
                                x + columnWidth - 20 - powerWidth,
                                top + 26
                        );
                }
        }

        /**
         * Draws the reserve monster grid.
         *
         * @param graphics graphics context
         * @param party player party
         * @param menuX horizontal starting position
         * @param gridTop vertical starting position
         * @param columnWidth column width
         * @param cursor selected cell
         */
        private void drawReserveGrid(
                Graphics2D graphics,
                BattleParty party,
                int menuX,
                int gridTop,
                int columnWidth,
                int cursor) {

                for (int i = 0; i < party.getReserveCount(); i++) {
                        BattleMonster member = party.getReserveMember(i);

                        int x = menuX + (i % 2) * columnWidth;
                        int top = gridTop + (i / 2) * 44;

                        drawGridCell(
                                graphics,
                                x,
                                top,
                                columnWidth,
                                i == cursor
                        );

                        graphics.setColor(
                                member.isFainted() ? DIM_TEXT : TEXT
                        );

                        graphics.setFont(TITLE_FONT);

                        graphics.drawString(
                                member.getName(),
                                x,
                                top + 17
                        );

                        graphics.setFont(SMALL_FONT);

                        graphics.drawString(
                                member.isFainted()
                                        ? "Fainted"
                                        : "HP " + member.getCurrentHp()
                                        + " / " + member.getMaxHp(),
                                x,
                                top + 33
                        );
                }
        }

        /**
         * Draws a highlighted grid cell.
         *
         * @param graphics graphics context
         * @param x horizontal position
         * @param top vertical position
         * @param columnWidth cell width
         * @param selected true if selected
         */
        private void drawGridCell(
                Graphics2D graphics,
                int x,
                int top,
                int columnWidth,
                boolean selected) {

                if (selected) {
                        graphics.setColor(SELECTED);

                        graphics.fillRoundRect(
                                x - 10,
                                top,
                                columnWidth - 10,
                                40,
                                10,
                                10
                        );
                }
        }

        /**
         * Creates a stable placeholder color from a monster's name.
         *
         * @param monster monster to color
         * @return generated color
         */
        private Color colorFor(BattleMonster monster) {
                float hue =
                        Math.floorMod(monster.getName().hashCode(), 360) / 360f;

                return Color.getHSBColor(hue, 0.55f, 0.85f);
        }
}