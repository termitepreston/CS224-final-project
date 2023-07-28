package io.github.termitepreston.schoolprojects;

import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

/**
 * Hello world! (Swing style)
 *
 */
public class App {
    private static final String TITLE = "CS224 Final Project!";

    private static void createAndShowFrame() {
        // so this thing is called only once.
        System.out.println("App.createAndShowFrame()");
        JFrame frame = new JFrame(TITLE);
        var pane = frame.getContentPane();

        pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JMenuBar menuBar = new JMenuBar();
        JMenu c = new JMenu("File");
        c.add(new JMenuItem("Quit"));

        menuBar.add(c);

        class Model {
            private int clickCount = 0;

            public int getClickCount() {
                return clickCount;
            }

            public void setClickCount(int clickCount) {
                this.clickCount = clickCount;
            }

        }

        var m = new Model();

        JLabel label = new JLabel("Hello, world");
        JButton btn = new JButton("A Button!");
        btn.addActionListener(e -> {
            m.setClickCount(m.getClickCount() + 1);
            label.setText(String.format("total: %d", m.getClickCount()));
        });

        frame.setJMenuBar(menuBar);

        pane.add(label);
        pane.add(btn);

        frame.setMinimumSize(new Dimension(300, 300));
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowFrame());
    }
}
