package io.github.termitepreston.schoolprojects;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import io.github.termitepreston.schoolprojects.ui.ApplicationFrame;

import javax.swing.*;

public class Main {
    private static final String TITLE = "CS224 Final Project - Movie Management System";

    public static void main(String[] args) throws Exception {
        Config config = new Config("config.xml");

        DB db = new DB(config);

        FlatLaf.registerCustomDefaultsSource("io.github.termitepreston.schoolprojects.ui.themes");

        FlatMacDarkLaf.setup();

        SwingUtilities.invokeLater(() -> {
            new ApplicationFrame(TITLE, db);
        });
    }
}