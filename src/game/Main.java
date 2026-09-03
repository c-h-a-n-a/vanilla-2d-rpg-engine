package game;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Main {
    private static final int SCREEN_WIDTH = 960;
    private static final int SCREEN_HEIGHT = 540;

    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::startGame);
    }

    private static void startGame() {
        KeyboardInput keyboardInput = new KeyboardInput();

        GameCanvas gameCanvas = new GameCanvas(
                SCREEN_WIDTH,
                SCREEN_HEIGHT,
                keyboardInput
        );

        GameLoop gameLoop = new GameLoop(gameCanvas);

        JFrame window = new JFrame("My 2D RPG");

        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        window.setResizable(false);
        window.add(gameCanvas);

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

        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                gameLoop.stop();
                window.dispose();
            }
        });

        gameLoop.start();
    }
}
