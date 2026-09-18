package game;

import java.awt.*;
import java.awt.image.BufferStrategy;

public class GameCanvas extends Canvas {

    //logging fps
    private int currentFps;
    private int framesThisSecond;
    private long fpsTimer = System.nanoTime();



    public GameCanvas(int width, int height) {

        setPreferredSize(new Dimension(width, height));
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        /*
         * The game performs active rendering through
         * BufferStrategy instead of AWT repaint events.
         */
        setIgnoreRepaint(true);
    }

    public void initialize() {
        if (!isDisplayable()) {
            throw new IllegalStateException(
                    "GameCanvas must be visible before initialization."
            );
        }

        createBufferStrategy(3);
        requestFocusInWindow();
    }


    public void render(GameRenderer renderer) {

        BufferStrategy bufferStrategy = getBufferStrategy();

        if (bufferStrategy == null) {
            return;
        }

        do {
            do {
                Graphics2D graphics =
                        (Graphics2D) bufferStrategy.getDrawGraphics();

                try {
                    renderer.render(
                            graphics,
                            getWidth(),
                            getHeight(),
                            currentFps
                    );
                } finally {
                    graphics.dispose();
                }

            } while (bufferStrategy.contentsRestored());

            bufferStrategy.show();

        } while (bufferStrategy.contentsLost());

        /*
         * Helps synchronize graphics on some operating systems,
         * especially Linux.
         */
        Toolkit.getDefaultToolkit().sync();

        /*
         * A complete frame was rendered and displayed.
         */
        updateFpsCounter();
    }


    private void updateFpsCounter() {
        framesThisSecond++;

        long currentTime = System.nanoTime();
        long elapsedTime = currentTime - fpsTimer;

        /*
         * One second has passed.
         */
        if (elapsedTime >= 1_000_000_000L) {
            currentFps = framesThisSecond;
            framesThisSecond = 0;
            fpsTimer = currentTime;
        }
    }
}
