package game;

import java.util.concurrent.locks.LockSupport;

public class GameLoop implements Runnable{

    private static final double TARGET_UPS = 60.0;
    private static final double TARGET_FPS = 60.0;

    private static final double NS_PER_UPDATE =
            1_000_000_000.0 / TARGET_UPS;

    private static final double NS_PER_FRAME =
            1_000_000_000.0 / TARGET_FPS;

    private static final double SECONDS_PER_UPDATE =
            1.0 / TARGET_UPS;

    private static final int MAX_UPDATES_PER_CYCLE = 5;

    private static final long MAX_ELAPSED_TIME =
            250_000_000L;

    private final Game game;
    private final GameCanvas gameCanvas;
    private final GameRenderer renderer;

    private Thread gameThread;
    private volatile boolean running;

    public GameLoop(
            Game game,
            GameCanvas gameCanvas,
            GameRenderer renderer) {
        this.game = game;
        this.gameCanvas = gameCanvas;
        this.renderer = renderer;
    }

    public synchronized void start() {
        if (running) {
            return;
        }

        running = true;

        gameThread = new Thread(this, "Game Loop");
        gameThread.start();
    }

    public void stop() {
        Thread threadToJoin;

        synchronized (this) {
            if (!running) {
                return;
            }

            running = false;
            threadToJoin = gameThread;
        }

        if (threadToJoin == null
                || Thread.currentThread() == threadToJoin) {
            return;
        }

        try {
            threadToJoin.join();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        long previousTime = System.nanoTime();

        double updateDelta = 0;
        double frameDelta = 0;

        while (running) {
            long currentTime = System.nanoTime();
            long elapsedTime = currentTime - previousTime;

            previousTime = currentTime;

            /*
             * Prevents a long pause, such as stopping at a
             * debugger breakpoint, from creating hundreds
             * of delayed updates.
             */
            elapsedTime = Math.min(
                    elapsedTime,
                    MAX_ELAPSED_TIME
            );

            updateDelta += elapsedTime / NS_PER_UPDATE;
            frameDelta += elapsedTime / NS_PER_FRAME;

            int updatesPerformed = 0;

            while (updateDelta >= 1
                    && updatesPerformed < MAX_UPDATES_PER_CYCLE) {

                game.update(
                        SECONDS_PER_UPDATE,
                        gameCanvas.getWidth(),
                        gameCanvas.getHeight());

                updateDelta--;
                updatesPerformed++;
            }

            if (updatesPerformed == MAX_UPDATES_PER_CYCLE
                    && updateDelta >= 1) {
                updateDelta = 0;
            }

            if (frameDelta >= 1) {
                gameCanvas.render(renderer);

                /*
                 * Discards missed full frames but preserves
                 * the fractional frame time.
                 */
                frameDelta %= 1;
            }

            /*
             * Avoids continuously consuming an entire
             * processor core.
             */
            LockSupport.parkNanos(1_000_000);
        }
    }
}
