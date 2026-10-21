package game.render;

import game.assets.AssetManager;
import game.entity.Npc;
import game.state.DialogueManager;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Draws the dialogue box: portrait, speaker name, wrapped text and hint.
 */
public class DialogueRenderer {

    private static final Font NAME_FONT = new Font("Monospaced", Font.BOLD, 18);
    private static final Font TEXT_FONT = new Font("Monospaced", Font.PLAIN, 15);
    private static final Font HINT_FONT = new Font("Monospaced", Font.PLAIN, 12);
    private static final Font INITIAL_FONT = new Font("Monospaced", Font.BOLD, 20);

    private static final Color BOX_BACKGROUND = new Color(15, 20, 30, 235);
    private static final Color BOX_BORDER = new Color(230, 230, 240);
    private static final Color NAME_COLOR = new Color(255, 220, 120);
    private static final Color HINT_COLOR = new Color(200, 200, 200);
    private static final Color PLACEHOLDER_COLOR = new Color(65, 80, 105);

    private static final int MARGIN = 24;
    private static final int BOX_HEIGHT = 140;
    private static final int PORTRAIT_SIZE = 128;
    private static final int LINE_HEIGHT = 21;

    // Wrapping the text is relatively expensive, so only redo it when the text changes.
    private String cachedText;
    private int cachedWidth = -1;
    private List<String> cachedLines = List.of();

    public void render(Graphics2D graphics, DialogueManager dialogue, int screenWidth, int screenHeight) {
        if (!dialogue.isActive()) {
            return;
        }

        Npc npc = dialogue.getSpeaker();

        int boxX = MARGIN;
        int boxY = screenHeight - BOX_HEIGHT - MARGIN;
        int boxWidth = screenWidth - MARGIN * 2;

        graphics.setColor(BOX_BACKGROUND);
        graphics.fillRoundRect(boxX, boxY, boxWidth, BOX_HEIGHT, 16, 16);

        graphics.setColor(BOX_BORDER);
        graphics.drawRoundRect(boxX, boxY, boxWidth, BOX_HEIGHT, 16, 16);

        int portraitX = boxX + 18;
        int portraitY = boxY + 24;

        drawPortrait(graphics, npc, portraitX, portraitY);

        int textX = portraitX + PORTRAIT_SIZE + 20;
        int textWidth = boxX + boxWidth - textX - 20;

        graphics.setFont(NAME_FONT);
        graphics.setColor(NAME_COLOR);
        graphics.drawString(npc.getName(), textX, boxY + 34);

        graphics.setFont(TEXT_FONT);
        graphics.setColor(Color.WHITE);

        int y = boxY + 62;

        for (String line : wrap(graphics, dialogue.getCurrentLine(), textWidth)) {
            graphics.drawString(line, textX, y);
            y += LINE_HEIGHT;
        }

        graphics.setFont(HINT_FONT);
        graphics.setColor(HINT_COLOR);
        graphics.drawString(
                dialogue.isOnLastLine() ? "E - Close" : "E - Next",
                boxX + boxWidth - 100,
                boxY + BOX_HEIGHT - 14
        );
    }

    private void drawPortrait(Graphics2D graphics, Npc npc, int x, int y) {
        BufferedImage portrait = AssetManager.scaledImageOrNull(
                npc.getPortraitPath(), PORTRAIT_SIZE, PORTRAIT_SIZE);

        if (portrait != null) {
            graphics.drawImage(portrait, x, y, null);
            return;
        }

        // Placeholder: a box with the NPC's first letter.
        graphics.setColor(PLACEHOLDER_COLOR);
        graphics.fillRoundRect(x, y, PORTRAIT_SIZE, PORTRAIT_SIZE, 10, 10);

        graphics.setColor(Color.WHITE);
        graphics.setFont(INITIAL_FONT);

        String initial = npc.getName().isBlank() ? "?" : npc.getName().substring(0, 1).toUpperCase();

        graphics.drawString(initial, x + PORTRAIT_SIZE / 2 - 6, y + PORTRAIT_SIZE / 2 + 7);
    }

    /** Splits text into lines that fit maxWidth. Uses the graphics' current font. */
    private List<String> wrap(Graphics2D graphics, String text, int maxWidth) {
        if (text == null) {
            return List.of();
        }

        if (text.equals(cachedText) && maxWidth == cachedWidth) {
            return cachedLines;
        }

        FontMetrics metrics = graphics.getFontMetrics();
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();

        for (String word : text.split("\\s+")) {
            int lengthBefore = line.length();

            if (!line.isEmpty()) {
                line.append(' ');
            }
            line.append(word);

            if (metrics.stringWidth(line.toString()) > maxWidth && lengthBefore > 0) {
                // The word didn't fit: finish the previous line and start a new one.
                line.setLength(lengthBefore);
                lines.add(line.toString());
                line.setLength(0);
                line.append(word);
            }
        }

        if (!line.isEmpty()) {
            lines.add(line.toString());
        }

        cachedText = text;
        cachedWidth = maxWidth;
        cachedLines = lines;

        return lines;
    }
}
