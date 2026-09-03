package game;

import javax.swing.*;
import java.awt.*;

public class Main {
    private static final int SCREEN_WIDTH = 960;
    private static final int SCREEN_HEIGHT = 540;

    private Main() {
    }

    public static void main(String[] args) {
        /*
         * Keep Java's coordinate scale consistent when moving
         * between monitors with different Windows scaling.
         *
         * This must run before Swing or AWT creates a window.
         */
        System.setProperty("sun.java2d.uiScale", "1.5");
        SwingUtilities.invokeLater(Main::startGame);
    }

    private static void startGame() {
        KeyboardInput keyboardInput = new KeyboardInput();

        GameCanvas gameCanvas = new GameCanvas(
                SCREEN_WIDTH,
                SCREEN_HEIGHT
        );

        gameCanvas.addKeyListener(keyboardInput);
        gameCanvas.addFocusListener(keyboardInput);

        Game game = new Game(keyboardInput);
        GameRenderer renderer = new GameRenderer(game);

        GameLoop gameLoop = new GameLoop(
                game,
                gameCanvas,
                renderer
        );

        JFrame window = new JFrame("My 2D RPG");

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLayout(new BorderLayout());

        /*
         * If Windows briefly exposes the frame background,
         * it will be black instead of white.
         */
        window.getContentPane().setBackground(Color.BLACK);
        window.add(gameCanvas, BorderLayout.CENTER);
        window.setResizable(false);


        /*
         * Sizes the JFrame according to the preferred
         * size of the GameCanvas.
         */
        window.pack();

        window.setLocationRelativeTo(null);
        window.setVisible(true);

        /*
         * The canvas must already be visible before its
         * BufferStrategy can be created.
         */
        gameCanvas.initialize();


        gameLoop.start();
    }
}
