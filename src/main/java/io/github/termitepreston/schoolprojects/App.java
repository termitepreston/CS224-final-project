package io.github.termitepreston.schoolprojects;

import io.github.termitepreston.schoolprojects.ui.SortAndReduce;

public class App {
    private static final String TITLE = "CS224 Final Project - GridBagLayout Demo";

    public static void main(String[] args) throws Exception {
        Config config = new Config("config.xml");

        DB db = new DB(config);
    }

    private static void createAndShowFrame() {
        var frame = new SortAndReduce(TITLE);

        frame.pack();
        frame.setVisible(true);
    }

}