package game.state;

import game.entity.Npc;

/**
 * Tracks the current conversation: who is talking and which line is shown.
 */
public final class DialogueManager {

    private Npc speaker;
    private int lineIndex;

    public boolean isActive() {
        return speaker != null;
    }

    public void start(Npc npc) {
        this.speaker = npc;
        this.lineIndex = 0;
    }

    /** Shows the next line, or ends the conversation after the last one. */
    public void advance() {
        if (speaker == null) {
            return;
        }

        if (lineIndex < speaker.getDialogueLineCount() - 1) {
            lineIndex++;
        } else {
            end();
        }
    }

    public void end() {
        speaker = null;
        lineIndex = 0;
    }

    public boolean isOnLastLine() {
        return speaker != null && lineIndex == speaker.getDialogueLineCount() - 1;
    }

    public Npc getSpeaker() {
        return speaker;
    }

    /** The text to display, or null if nobody is talking. */
    public String getCurrentLine() {
        return speaker == null ? null : speaker.getDialogueLine(lineIndex);
    }
}
