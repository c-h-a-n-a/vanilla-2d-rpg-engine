package game;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

public class KeyboardInput implements KeyListener, FocusListener {

    private final Set<Integer> pressedKeys = new HashSet<>();
    @Override
    public void keyTyped(java.awt.event.KeyEvent e) {
        // Not used
    }

    @Override
    public synchronized void keyPressed(KeyEvent event) {
        pressedKeys.add(event.getKeyCode());
    }

    @Override
    public synchronized void keyReleased(KeyEvent event) {
        pressedKeys.remove(event.getKeyCode());
    }

    public synchronized boolean isPressed(int keyCode) {
        return pressedKeys.contains(keyCode);
    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        /*
         * Prevents a key from remaining "pressed" when
         * the player switches to another window.
         */
        pressedKeys.clear();
    }
}
