
package game.render;

import game.entity.Monster;
import game.entity.MonsterType;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class BattleRenderer {

        private static final Color BACKGROUND = new Color(25, 30, 48);
        private static final Color PANEL = new Color(235, 235, 220);
        private static final Color TEXT = new Color(35, 40, 50);

        public void render(
                        Graphics2D graphics,
                        int screenWidth,
                        int screenHeight,
                        Monster monster) {
                // Background.
                graphics.setColor(BACKGROUND);
                graphics.fillRect(0, 0, screenWidth, screenHeight);

                // Shared ground level for both sides.
                int groundY = screenHeight / 2 + 40;

                // Both platforms are on the same horizontal level.
                graphics.setColor(new Color(75, 110, 85));

                graphics.fillOval(
                                65,
                                groundY,
                                280,
                                65);

                graphics.fillOval(
                                screenWidth - 345,
                                groundY,
                                280,
                                65);

                // Player-side monster.
                drawCreature(
                                graphics,
                                150,
                                groundY - 85,
                                new Color(75, 145, 220));

                // Enemy monster.
                Color enemyColor = new Color(monster.getType().getRgb());

                drawCreature(
                                graphics,
                                screenWidth - 250,
                                groundY - 85,
                                enemyColor);

                // Enemy information panel.
                drawInfoPanel(
                                graphics,
                                35,
                                35,
                                240,
                                75,
                                monster.getType().getLabel());

                // Battle message panel.
                int panelY = screenHeight - 125;

                graphics.setColor(PANEL);
                graphics.fillRoundRect(
                                25,
                                panelY,
                                screenWidth - 50,
                                95,
                                20,
                                20);

                graphics.setColor(TEXT);
                graphics.setFont(new Font("Arial", Font.BOLD, 18));

                graphics.drawString(
                                "A wild " + monster.getType().getLabel()
                                                + " appeared!",
                                50,
                                panelY + 35);

                graphics.setFont(new Font("Arial", Font.PLAIN, 14));

                graphics.drawString(
                                "Battle screen prototype - Press ESC to return",
                                50,
                                panelY + 65);
        }

        private void drawCreature(
                        Graphics2D graphics,
                        int x,
                        int y,
                        Color color) {
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

        private void drawInfoPanel(
                        Graphics2D graphics,
                        int x,
                        int y,
                        int width,
                        int height,
                        String monsterName) {
                graphics.setColor(PANEL);
                graphics.fillRoundRect(x, y, width, height, 15, 15);

                graphics.setColor(TEXT);
                graphics.setFont(new Font("Arial", Font.BOLD, 18));

                graphics.drawString(monsterName, x + 15, y + 30);

                graphics.setFont(new Font("Arial", Font.PLAIN, 13));
                graphics.drawString("Wild monster", x + 15, y + 53);
        }
}
