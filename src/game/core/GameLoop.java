package game.core;

import game.state.Game;
import game.config.GameConfig;

import java.util.concurrent.locks.LockSupport;

public class GameLoop implements Runnable {

    private static final double NS_PER_UPDATE = 1_000_000_000.0 / GameConfig.TARGET_UPS;
    private static final double NS_PER_FRAME = 1_000_000_000.0 / GameConfig.TARGET_FPS;
    private static final double SECONDS_PER_UPDATE = 1.0 / GameConfig.TARGET_UPS;

    private static final int MAX_UPDATES_PER_CYCLE = 5;

    /**
     * Caps one loop step so a debugger pause doesn't cause a burst of updates.
     */
    private static final long MAX_ELAPSED_TIME = 250_000_000L;

    private static final long STOP_TIMEOUT_MS = 1000;

    private final Game game;
    private final GameCanvas gameCanvas;
    private final FrameRenderer renderer;

    private Thread gameThread;
    private volatile boolean running;

    public GameLoop(Game game, GameCanvas gameCanvas, FrameRenderer renderer) {
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

        if (threadToJoin == null || Thread.currentThread() == threadToJoin) {
            return;
        }

        try {
            threadToJoin.join(STOP_TIMEOUT_MS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        try {
            loop();
        } catch (Throwable error) {
            // Without this, an exception would kill the thread silently
            // and leave a frozen window on screen.
            System.err.println("Game loop crashed:");
            error.printStackTrace();
            running = false;
        }
    }

    private void loop() {
        long previousTime = System.nanoTime();

        double updateDelta = 0;
        double frameDelta = 0;

        while (running) {
            long currentTime = System.nanoTime();
            long elapsedTime = Math.min(currentTime - previousTime, MAX_ELAPSED_TIME);

            previousTime = currentTime;

            updateDelta += elapsedTime / NS_PER_UPDATE;
            frameDelta += elapsedTime / NS_PER_FRAME;

            int updatesPerformed = 0;

            while (updateDelta >= 1 && updatesPerformed < MAX_UPDATES_PER_CYCLE) {
                game.update(SECONDS_PER_UPDATE);

                updateDelta--;
                updatesPerformed++;
            }

            // Too far behind: drop the backlog instead of spiralling.
            if (updatesPerformed == MAX_UPDATES_PER_CYCLE && updateDelta >= 1) {
                updateDelta = 0;
            }

            if (frameDelta >= 1) {
                gameCanvas.render(renderer);
                frameDelta %= 1;
            }

            // Avoid burning a whole CPU core.
            LockSupport.parkNanos(1_000_000);
        }
    }
}
