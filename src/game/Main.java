package game;

import game.config.GameConfig;
import game.core.GameCanvas;
import game.core.GameLoop;
import game.core.GameWindow;
import game.input.KeyboardInput;
import game.render.GameRenderer;
import game.state.Game;

import javax.swing.SwingUtilities;

public class Main {

    private Main() {
    }

    public static void main(String[] args) {
        // Uncomment to force a UI scale (must run before Swing/AWT creates a window):
        // System.setProperty("sun.java2d.uiScale", "1.5");
        SwingUtilities.invokeLater(Main::startGame);
    }

    private static void startGame() {
        KeyboardInput input = new KeyboardInput();

        GameCanvas canvas = new GameCanvas(GameConfig.SCREEN_WIDTH, GameConfig.SCREEN_HEIGHT);
        canvas.addKeyListener(input);
        canvas.addFocusListener(input);

        Game game = new Game(input);
        GameRenderer renderer = new GameRenderer(game);
        GameLoop loop = new GameLoop(game, canvas, renderer);

        GameWindow window = new GameWindow(GameConfig.WINDOW_TITLE, canvas, loop::stop);
        window.open();

        // The canvas must be visible before its BufferStrategy can be created.
        canvas.initialize();

        loop.start();
    }
}
