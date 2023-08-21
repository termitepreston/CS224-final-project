package io.github.termitepreston.schoolprojects;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import io.github.termitepreston.schoolprojects.ui.SortAndReduce;

import javax.swing.*;

public class App {
    private static final String TITLE = "CS224 Final Project - GridBagLayout Demo";

    public static void main(String[] args) throws Exception {
        Config config = new Config("config.xml");

        DB db = new DB(config);

        FlatLaf.registerCustomDefaultsSource("io.github.termitepreston.schoolprojects.ui.themes");

        FlatMacDarkLaf.setup();

        SwingUtilities.invokeLater(App::createAndShowFrame);
    }

    private static void createAndShowFrame() {
        var frame = new SortAndReduce(TITLE);

        frame.pack();
        frame.setVisible(true);
    }

}