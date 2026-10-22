package game.core;

import javax.swing.JFrame;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * The application window. Keeps all JFrame setup out of Main.
 */
public final class GameWindow {

    private final JFrame frame;

    /**
     * @param onClose runs when the user closes the window (e.g. stop the game loop)
     */
    public GameWindow(String title, GameCanvas canvas, Runnable onClose) {
        frame = new JFrame(title);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                onClose.run();
            }
        });

        frame.setLayout(new BorderLayout());

        // If the OS briefly exposes the frame background, show black, not white.
        frame.getContentPane().setBackground(Color.BLACK);
        frame.add(canvas, BorderLayout.CENTER);
        frame.setResizable(false);
    }

    /** Sizes the window to the canvas, centres it and shows it. */
    public void open() {
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
