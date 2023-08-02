package io.github.termitepreston.schoolprojects;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import io.github.termitepreston.schoolprojects.ui.SortAndReduce;

public class App {
    private static final String TITLE = "CS224 Final Project - GridBagLayout Demo";

    public static void main(String[] args) throws SQLException {
        // SwingUtilities.invokeLater(() -> createAndShowFrame());
        dbTest();
    }

    private static void createAndShowFrame() {
        SortAndReduce frame = new SortAndReduce(TITLE);

        frame.pack();
        frame.setVisible(true);
    }

    private static void dbTest() throws SQLException {
        var conn = getConn();

        var statStr = "select * from greetings";

        var stat = conn.createStatement();

        stat.executeQuery(statStr);

        try (ResultSet rs = stat.getResultSet()) {
            while (rs.next()) {
                System.out.println(rs.getString(2));
            }
        }
    }

    private static Connection getConn() throws SQLException {
        return DriverManager
                .getConnection("jdbc:sqlserver://localhost;encrypt=false;user=app;password=app;databaseName=project");
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
        setPreferredSize(new Dimension(280, 360));
        setResizable(false);
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

        var buttons = new JPanel();

        c.weighty = 0.8;
        // c.anchor = GridBagConstraints.CENTER;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        pane.add(counterLabel, c);

        // ActionListener is a "Functional Interface"
        counterButton.addActionListener(e -> {
            data.increment();

            counterLabel.setText(String.format("Count: %d", data.getCount()));
            System.out.printf("Is this lambda being executed on the event dispatch theads? %s\n",
                    SwingUtilities.isEventDispatchThread() ? "YES" : "NO");

        });
        buttons.add(counterButton);

        var swingWorker = new SwingWorker<String[], Void>() {

            @Override
            protected String[] doInBackground() throws Exception {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'doInBackground'");
            }

        };

        // Experiment (mutate ui state from a non-edt)
        (new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }

                counterLabel.setText(String.format("Reset from another thread",
                        data.getCount()));

                System.out.printf("Is this lambda being executed on the event dispatch theads? %s\n",
                        SwingUtilities.isEventDispatchThread() ? "YES" : "NO");
            }

        })).start();

        resetButton.addActionListener(e -> {
            data.reset();

            counterLabel.setText("Click to COUNT!");
        });
        buttons.add(resetButton);

        // Add the buttons to the gridbaglayout.
        c.gridx = 0;
        c.gridy = 1;
        pane.add(buttons, c);
    }
}
