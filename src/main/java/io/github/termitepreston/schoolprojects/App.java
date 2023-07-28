package io.github.termitepreston.schoolprojects;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

public class App {
    private static final String TITLE = "CS224 Final Project - GridBagLayout Demo";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowFrame());
    }

    private static void createAndShowFrame() {
        Data appData = new Data();
        MainFrame frame = new MainFrame(TITLE, appData);

        frame.pack();
        frame.setVisible(true);
    }
}

/**
 * Hello world! (Swing style)
 *
 */

class Data {
    private int count = 0;

    public Data() {
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void increment() {
        count++;
    }

    public void reset() {
        count = 0;
    }
}

class MainFrame extends JFrame {
    private Data data;
    private JLabel counterLabel;
    private JButton counterButton;
    private JButton resetButton;

    public MainFrame(String title, Data data) {
        super(title);

        this.data = data;

        counterLabel = new JLabel("Click to Count!");
        counterLabel.setFont(new Font("Inter", Font.BOLD, 32));

        counterButton = new JButton("COUNT!");
        resetButton = new JButton("RESET!");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(360, 540));
        buildUI();
    }

    public JLabel getCounterLabel() {
        return counterLabel;
    }

    public void setCounterLabel(JLabel counterLabel) {
        this.counterLabel = counterLabel;
    }

    public JButton getCounterButton() {
        return counterButton;
    }

    public void setCounterButton(JButton counterButton) {
        this.counterButton = counterButton;
    }

    private void buildUI() {
        var pane = getContentPane();

        pane.setLayout(new GridBagLayout());

        var c = new GridBagConstraints();

        c.weighty = 0.8;
        // c.anchor = GridBagConstraints.CENTER;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        pane.add(counterLabel, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.5;
        c.anchor = GridBagConstraints.LINE_START;
        // ActionListener is a "Functional Interface"
        counterButton.addActionListener(e -> {
            data.increment();

            counterLabel.setText(String.format("Count: %d", data.getCount()));
        });
        pane.add(counterButton, c);

        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 0.5;
        c.anchor = GridBagConstraints.LINE_END;
        resetButton.addActionListener(e -> {
            data.reset();

            counterLabel.setText("Click to COUNT!");
        });
        pane.add(resetButton, c);
    }
}
