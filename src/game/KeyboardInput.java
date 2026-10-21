
package game;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

public class KeyboardInput implements KeyListener, FocusListener {

    private final Set<Integer> pressedKeys = new HashSet<>();
    private final Set<Integer> justPressedKeys = new HashSet<>();

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used.
    }

    @Override
    public synchronized void keyPressed(KeyEvent event) {
        int keyCode = event.getKeyCode();

        // Only register a new press if the key wasn't already held.
        if (pressedKeys.add(keyCode)) {
            justPressedKeys.add(keyCode);
        }
    }

    @Override
    public synchronized void keyReleased(KeyEvent event) {
        pressedKeys.remove(event.getKeyCode());
    }

    public synchronized boolean isPressed(int keyCode) {
        return pressedKeys.contains(keyCode);
    }

    /**
     * Returns true once for each new key press.
     * The press is consumed when this method returns true.
     */
    public synchronized boolean consumeJustPressed(int keyCode) {
        return justPressedKeys.remove(keyCode);
    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public synchronized void focusLost(FocusEvent e) {
        pressedKeys.clear();
        justPressedKeys.clear();
    }
}
