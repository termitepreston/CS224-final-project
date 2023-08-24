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

    private JLabel feedbackLabel;

    public AuthDialog(ApplicationFrame parent, DB db) {
        super(parent);

        this.parent = parent;
        this.db = db;

        buildUI();
    }

    private void buildUI() {
        setTitle("Sign in...");

        setLayout(new MigLayout());

        GridBagConstraints c = new GridBagConstraints();

        // description
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 1;
        c.insets = new Insets(12, 12, 12, 12);
        c.anchor = GridBagConstraints.FIRST_LINE_START;

        // description
        var descLabel = new JLabel("""
                <html>
                <p>
                Enter appropriate username and password<br/>
                to access ADMIN console or press cancel<br/>
                to browse movies.
                </p>
                </html>
                """);
        descLabel.setFont(UIManager.getFont("h2.font"));
        add(descLabel, c);

        // feedback
        feedbackLabel = new JLabel("Make sure username password combination is correct!");
        c.gridy = 1;
        c.anchor = GridBagConstraints.CENTER;
        add(feedbackLabel, c);


        // username field
        // username entry
        JPanel usernameField = new JPanel();
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setAlignmentX(LEFT_ALIGNMENT);
        int inputWidth = 20;
        usernameInput = new JTextField(inputWidth);
        usernameInput.setAlignmentX(LEFT_ALIGNMENT);
        usernameField.setLayout(new BoxLayout(usernameField, BoxLayout.PAGE_AXIS));
        usernameField.add(usernameLabel);
        usernameField.add(Box.createRigidArea(new Dimension(0, 4)));
        usernameField.add(usernameInput);

        // event stuff
        usernameInput.getDocument().addDocumentListener(this);

        // layout
        c.gridy = 2;
        c.insets = new Insets(12, 12, 12, 12);
        c.fill = GridBagConstraints.HORIZONTAL;
        add(usernameField, c);

        // password field.
        JPanel passwordField = new JPanel();
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setAlignmentX(LEFT_ALIGNMENT);
        passwordInput = new JPasswordField(inputWidth);
        passwordInput.setAlignmentX(LEFT_ALIGNMENT);
        passwordField.setLayout(new BoxLayout(passwordField, BoxLayout.PAGE_AXIS));
        passwordField.add(passwordLabel);
        passwordField.add(Box.createRigidArea(new Dimension(0, 4)));
        passwordField.add(passwordInput);

        // event
        passwordInput.getDocument().addDocumentListener(this);


        // layout
        c.gridy = 3;
        add(passwordField, c);

        // sign in button
        signInBtn = new JButton("Sign in...");
        c.gridy = 4;
        c.gridx = 0;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.LINE_START;
        c.ipady = 2;
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0;

        // event stuff
        signInBtn.setEnabled(false);
        signInBtn.addActionListener(this);

        add(signInBtn, c);

        // cancel button
        cancelBtn = new JButton("Cancel");
        c.gridx = 1;
        c.anchor = GridBagConstraints.LINE_END;
        c.weightx = 0;
        add(cancelBtn, c);

        // evt handling
        cancelBtn.addActionListener(this);

        pack();
        setResizable(false);
        setModal(true);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == cancelBtn) {
            parent.setCurrentUser(null);
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
                        User user = get();


                        feedbackLabel.setText(String.format("Welcome, %s", user.getUsername()));
                        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                        parent.setCurrentUser(user);
                        dispose();

                    } catch (ExecutionException e) {
                        feedbackLabel.setText(String.format("Failed! reason: %s", e.getMessage()));

                        signInBtn.setEnabled(true);
                        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

                    } catch (InterruptedException e) {
                        e.printStackTrace();
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
