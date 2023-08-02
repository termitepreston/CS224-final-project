package io.github.termitepreston.schoolprojects.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

public class SortAndReduce extends JFrame {
    private static final int SIZE = 30000000;
    private static final String INSTRUCTIONS_TMPL = """
            Press CALCULATE button to generate a random array of
            %d integers, sort them and then, finally, return their sum.
            """;
    private static final String RESULTS_TMPL = """
            The generated array (which has %d elements) has a
            sum of %d.
            """;

    private final JButton calculateBtn;
    private final JTextArea resultsText;

    public SortAndReduce(String title) {
        super(title);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        resultsText = new JTextArea(String.format(INSTRUCTIONS_TMPL, SIZE));
        resultsText.setFont(new Font("Inter", Font.PLAIN, 28));
        calculateBtn = new JButton("Calculate");

        buildUI();
        wireUpEvents();
    }

    private void buildUI() {
        var pane = this.getContentPane();

        pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));

        pane.add(resultsText);
        pane.add(calculateBtn);

        pane.setMinimumSize(new Dimension(400, 500));
    }

    private void wireUpEvents() {
        calculateBtn.addActionListener(e -> (new SwingWorker<Long, Void>() {

            @Override
            protected Long doInBackground() throws Exception {
                int[] arr = new int[SIZE];

                for (int i = 0; i < arr.length; i++) {
                    arr[i] = ThreadLocalRandom.current().nextInt(100);
                }

                Arrays.sort(arr);

                long sum = 0;
                for (int i = 0; i < arr.length; i++) {
                    sum += arr[i];
                }

                return sum;
            }

            @Override
            protected void done() {
                try {
                    resultsText.setText(String.format(RESULTS_TMPL, SIZE, get()));
                } catch (Exception ignore) {
                    ignore.printStackTrace();
                }
            }

        }).execute());
    }
}
