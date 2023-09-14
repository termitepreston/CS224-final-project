package io.github.termitepreston.schoolprojects.ui;

import io.github.termitepreston.schoolprojects.DB;
import io.github.termitepreston.schoolprojects.model.User;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutionException;

public class AuthDialog extends JDialog implements ActionListener, DocumentListener {
    private final ApplicationFrame parent;
    private final DB db;
    private JTextField usernameInput, passwordInput;
    private JButton signInBtn, cancelBtn;
    private User user;

    public AuthDialog(ApplicationFrame parent, DB db) {
        super(parent);

        this.parent = parent;
        this.db = db;

        setModal(true);

        buildUI();
    }

    public static User login(ApplicationFrame parent, DB db) {
        var ad = new AuthDialog(parent, db);

        return ad.getUser();
    }

    public User getUser() {
        return user;
    }

    private void buildUI() {
        setTitle("Sign in...");

        setLayout(new MigLayout("insets 16",
                "[left][right]",
                "8[]16[]16[]16[]16[]8"));


        // description

        var descLabel = new JLabel("""
                <html>
                <p>
                Enter appropriate username and password
                to access ADMIN console or press cancel
                to browse movies.
                </p>
                </html>
                """);
        descLabel.setFont(UIManager.getFont("h2.font"));
        add(descLabel, "w 300!, h 100!, wrap, span, align center");

        // feedback
        add(new JLabel("<html>Make sure username password combination is correct!</html>"), "wrap, span, align center");


        // username field
        // username entry
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setAlignmentX(LEFT_ALIGNMENT);
        int inputWidth = 20;
        usernameInput = new JTextField(inputWidth);
        usernameInput.setAlignmentX(LEFT_ALIGNMENT);

        // event stuff
        usernameInput.getDocument().addDocumentListener(this);

        // layout
        add(usernameLabel);
        add(usernameInput, "wrap, grow");

        // password field.
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setAlignmentX(LEFT_ALIGNMENT);
        passwordInput = new JPasswordField(inputWidth);
        passwordInput.setAlignmentX(LEFT_ALIGNMENT);

        // event
        passwordInput.getDocument().addDocumentListener(this);
        // layout
        add(passwordLabel);
        add(passwordInput, "wrap, grow");

        // sign in button
        signInBtn = new JButton("Sign in...");

        // event stuff
        signInBtn.setEnabled(false);
        signInBtn.addActionListener(this);

        add(signInBtn);

        // cancel button
        cancelBtn = new JButton("Cancel");

        // evt handling
        cancelBtn.addActionListener(this);

        add(cancelBtn);

        setResizable(false);
        setModal(true);
        pack();
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cancelBtn) {
            user = null;
            dispose();
        }

        if (e.getSource() == signInBtn) {
            String username = usernameInput.getText();
            String password = passwordInput.getText();

            signInBtn.setEnabled(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            (new SwingWorker<User, Void>() {

                @Override
                protected User doInBackground() throws Exception {
                    User user = new User(db);

                    user.login(username, password);

                    return user;
                }

                @Override
                protected void done() {
                    try {
                        user = get();

                        JOptionPane.showMessageDialog(AuthDialog.this,
                                "Login successful!", "Success!", JOptionPane.INFORMATION_MESSAGE);

                        dispose();

                    } catch (ExecutionException | InterruptedException e) {
                        user = null;

                        JOptionPane.showMessageDialog(AuthDialog.this,
                                e.getCause().getMessage(), "Fatal Error", JOptionPane.ERROR_MESSAGE);

                        dispose();

                    } finally {
                        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                    }
                }
            }).execute();

        }
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        if (!usernameInput.getText().isEmpty() &&
                !passwordInput.getText().isEmpty()) {
            signInBtn.setEnabled(true);
        }


    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        if (usernameInput.getText().isEmpty() ||
                passwordInput.getText().isEmpty()) {
            signInBtn.setEnabled(false);
        }
    }

    @Override
    public void changedUpdate(DocumentEvent e) {

    }
}
