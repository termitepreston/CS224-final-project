package io.github.termitepreston.schoolprojects.ui;

import io.github.termitepreston.schoolprojects.DB;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ApplicationFrame extends JFrame implements PropertyChangeListener, ActionListener {
    private final DB db;
    /*
    Main application window menu elements.
     */
    JMenuBar menuBar;
    JMenu fileMenu, helpMenu;
    JMenuItem quitMenuItem, aboutMenuItem;

    private ProgressMonitor progressMonitor;
    private DBConnTestWorker worker;


    public ApplicationFrame(String title, DB db) {
        super(title);

        this.db = db;

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setPreferredSize(new Dimension(400, 400));

        buildMenuBar();

        // checkDBConn();

        pack();
        setVisible(true);

        new AuthDialog(this, db);
    }

    private void buildMenuBar() {
        menuBar = new JMenuBar();

        // File menu
        fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

        // File > Quit menu item.
        quitMenuItem = new JMenuItem("Quit", KeyEvent.VK_Q);
        quitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        quitMenuItem.addActionListener(this);
        fileMenu.add(quitMenuItem);

        // Help menu
        helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);
        menuBar.add(helpMenu);

        // Help > About menu item.
        aboutMenuItem = new JMenuItem("About", KeyEvent.VK_A);
        aboutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
        aboutMenuItem.addActionListener(this);
        helpMenu.add(aboutMenuItem);

        setJMenuBar(menuBar);
    }

    public ProgressMonitor getProgressMonitor() {
        return progressMonitor;
    }

    public void setProgressMonitor(ProgressMonitor progressMonitor) {
        this.progressMonitor = progressMonitor;
    }

    private void checkDBConn() {
        progressMonitor = new ProgressMonitor(this, "Connecting to a DB...",
                "", 0, 100);

        progressMonitor.setProgress(0);
        worker = new DBConnTestWorker(db, this);
        worker.addPropertyChangeListener(this);

        worker.execute();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress".equals(evt.getPropertyName())) {
            int progress = (Integer) evt.getNewValue();
            progressMonitor.setProgress(progress);
            String message =
                    String.format("Completed %d%%.\n", progress);
            progressMonitor.setNote(message);
            if (progressMonitor.isCanceled() || worker.isDone()) {
                Toolkit.getDefaultToolkit().beep();
                if (progressMonitor.isCanceled()) {
                    worker.cancel(true);
                }
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == aboutMenuItem) {
            JOptionPane.showMessageDialog(this,
                    """
                            CS224 final project: a film and subscriber management
                            application for small internet shops.
                            """, "About", JOptionPane.PLAIN_MESSAGE);
        }

        if (e.getSource() == quitMenuItem) {
            Object[] options = {"Yes, quit please", "No, I want to stay"};

            int n = JOptionPane.showOptionDialog(this,
                    "Are you sure you want to quit the application?",
                    "Exit application?",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1]);

            if (n == 0)
                dispose();
        }
    }
}
