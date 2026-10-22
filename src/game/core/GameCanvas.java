package game.core;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;

public class GameCanvas extends Canvas {

    private int currentFps;
    private int framesThisSecond;
    private long fpsTimer = System.nanoTime();

    public GameCanvas(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        // We render actively through a BufferStrategy, not AWT repaint events.
        setIgnoreRepaint(true);
    }

    /** The canvas must already be visible before calling this. */
    public void initialize() {
        if (!isDisplayable()) {
            throw new IllegalStateException(
                    "GameCanvas must be visible before initialization.");
        }

        createBufferStrategy(3);
        requestFocusInWindow();
    }

    public void render(FrameRenderer renderer) {
        BufferStrategy bufferStrategy = getBufferStrategy();

        if (bufferStrategy == null) {
            return;
        }

        do {
            do {
                Graphics2D graphics = (Graphics2D) bufferStrategy.getDrawGraphics();

                try {
                    renderer.render(graphics, getWidth(), getHeight(), currentFps);
                } finally {
                    graphics.dispose();
                }

            } while (bufferStrategy.contentsRestored());

            bufferStrategy.show();

        } while (bufferStrategy.contentsLost());

        // Helps synchronize graphics on some operating systems (e.g. Linux).
        Toolkit.getDefaultToolkit().sync();

        updateFpsCounter();
    }

    private void updateFpsCounter() {
        framesThisSecond++;

        long currentTime = System.nanoTime();

        if (currentTime - fpsTimer >= 1_000_000_000L) {
            currentFps = framesThisSecond;
            framesThisSecond = 0;
            fpsTimer = currentTime;
        }
    }
}
