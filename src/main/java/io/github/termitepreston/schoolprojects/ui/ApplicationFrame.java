package io.github.termitepreston.schoolprojects.ui;

import io.github.termitepreston.schoolprojects.DB;
import io.github.termitepreston.schoolprojects.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.Principal;

public class ApplicationFrame extends JFrame implements PropertyChangeListener, ActionListener {
    private final DB db;
    private JMenu fileMenu, helpMenu;

    // common menu items...
    private JMenuItem quitMenuItem, aboutMenuItem;

    // admin menu items
    private JMenu newMenu;
    private JMenuItem newMovieMenuItem;

    /*
    Main application window menu elements.
     */
    private JMenuBar menuBar;
    private Principal currentUser;

    public ApplicationFrame(String title, DB db) {
        super(title);

        this.db = db;

        addPropertyChangeListener(this);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setPreferredSize(new Dimension(400, 400));

        buildCommonMenuBar();

        pack();
        setVisible(true);

        showAuthDialog();

    }

    public Principal getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(Principal currentUser) {
        Principal oldUser = this.currentUser;

        this.currentUser = currentUser;

        firePropertyChange("currentUser", currentUser, oldUser);
    }

    private void buildAdminMenuBar() {
        newMenu = new JMenu("New...");
        newMenu.setMnemonic(KeyEvent.VK_E);
        fileMenu.add(newMenu);

        newMovieMenuItem = new JMenuItem("Movie");
        newMovieMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        newMovieMenuItem.addActionListener(this);
        newMenu.add(newMovieMenuItem);
    }

    private void buildCommonMenuBar() {
        menuBar = new JMenuBar();

        // File menu
        fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(fileMenu);

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

    private void addQuitMenuItem() {
        // File > Quit menu item.
        quitMenuItem = new JMenuItem("Quit", KeyEvent.VK_Q);
        quitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        quitMenuItem.addActionListener(this);
        fileMenu.add(quitMenuItem);

    }

    private void buildUI() {
        var pane = getContentPane();

        pane.setLayout(new GridBagLayout());

        var c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;

        var welcomeLabel = new JLabel();
        welcomeLabel.setFont(UIManager.getFont("h1.font"));

        if (currentUser != null && User.isAdmin(currentUser)) {
            buildAdminMenuBar();
            welcomeLabel.setText("Welcome, admin!");
        } else {
            welcomeLabel.setText("Welcome, user!");
        }

        addQuitMenuItem();

        pane.add(welcomeLabel, c);
        pack();
    }

    private void showAuthDialog() {
        new AuthDialog(this, db);
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

        if (e.getSource() == newMovieMenuItem) {
            var newMovieDialog = new NewMovieDialog("New movie...", db, 10, 25);
            newMovieDialog.setVisible(true);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("currentUser")) {
            buildUI();
        }
    }
}
