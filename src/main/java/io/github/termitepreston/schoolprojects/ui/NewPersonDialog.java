package io.github.termitepreston.schoolprojects.ui;

import io.github.termitepreston.schoolprojects.DB;
import io.github.termitepreston.schoolprojects.model.Person;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;

public class NewPersonDialog extends JDialog implements FocusListener, PropertyChangeListener {
    private final DB db;
    private final Status dataInsertStatus = Status.Idle;
    private final Insets fieldMargin = new Insets(20, 0, 0, 0);
    private final Insets labelMargin = new Insets(8, 0, 0, 0);
    private final Insets elementMargin = new Insets(4, 0, 0, 0);
    private final JLabel formTitleLabel = new JLabel();
    private final JLabel firstNameLabel = new JLabel("First name");
    private final JLabel firstNameHelperLabel = new JLabel("First name...");
    private final JTextField firstNameTF;
    private final JLabel firstNameErrorLabel = new JLabel("");
    private final JLabel lastNameLabel = new JLabel("Last name");
    private final JLabel lastNameHelperLabel = new JLabel("Last name...");
    private final JTextField lastNameTF;
    private final JLabel lastNameErrorLabel = new JLabel("");
    private final JButton submitBtn = new JButton();
    private final JPanel formContainer = new JPanel();
    private final HashMap<JComponent, Attribute> components = new HashMap<>();
    private final BiFunction<Class<? extends Person>, Pair<String, String>, SwingWorker<Person, Void>> personInserter;
    private Person person;

    public NewPersonDialog(DB db, Class<? extends Person> clazz, int cols) {
        this.db = db;
        firstNameTF = new JTextField(cols);
        lastNameTF = new JTextField(cols);
        setPreferredSize(new Dimension(384, 460));

        formTitleLabel.setText("New %s".formatted(clazz.getSimpleName()));
        submitBtn.setText("Add %s...".formatted(clazz.getSimpleName().toLowerCase()));

        // set the style of title labels.
        JLabel[] labels = {
                firstNameLabel,
                lastNameLabel,
        };
        Arrays.stream(labels).forEach(l -> l.putClientProperty("FlatLaf.style", "font: $h2.regular.font"));

        // set the style of helpers
        JLabel[] helpers = {
                firstNameHelperLabel,
                lastNameHelperLabel,
        };
        Arrays.stream(helpers).forEach(h -> h.putClientProperty("FlatLaf.style", "font: $medium.font"));

        // set the style of error labels.
        JLabel[] errors = {
                firstNameErrorLabel,
                lastNameErrorLabel,
        };
        Arrays.stream(errors).forEach(e -> {
            e.putClientProperty("FlatLaf.style", "font: $defaultFont; foreground: #d93b3b");
        });

        formTitleLabel.putClientProperty("FlatLaf.style", "font: $h1.font");

        personInserter = (c, p) -> new SwingWorker<>() {
            @Override
            protected Person doInBackground() throws Exception {
                return Person.insertOne(NewPersonDialog.this.db, c, p.first(), p.second());
            }

            @Override
            protected void done() {
                try {

                    setPerson(get());

                    JOptionPane.showMessageDialog(NewPersonDialog.this, "Person successfully inserted!");
                    dispose();

                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Function<String, Validator> required = prefix -> new Validator(o -> {
            if (o instanceof String[] arr)
                return arr.length > 0;

            if (o instanceof java.util.List list)
                return !list.isEmpty();

            if (o instanceof String s)
                return !s.isEmpty();

            return false;
        }, prefix + " is required!");

        components.put(firstNameTF, new Attribute(new Validator[]{required.apply("First name")}, firstNameLabel, firstNameHelperLabel, firstNameErrorLabel));
        components.put(lastNameTF, new Attribute(new Validator[]{required.apply("Last name")}, lastNameLabel, lastNameHelperLabel, lastNameErrorLabel));

        submitBtn.setEnabled(false);

        setTitle("New %s...".formatted(clazz.getSimpleName()));
        setModal(true);

        addPropertyChangeListener(this);

        formContainer.setLayout(new GridBagLayout());

        buildUI();
        wireUpUI();
    }

    public static Person createPerson(DB db, Class<? extends Person> clazz) {
        NewPersonDialog npd = new NewPersonDialog(db, clazz, 20);

        npd.setVisible(true);

        return npd.getPerson();
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    private void buildUI() {
        GridBagConstraints c = new GridBagConstraints();

        switch (dataInsertStatus) {
            case Idle -> {
                c.gridx = 0;
                c.gridy = 0;
                c.anchor = GridBagConstraints.CENTER;

                // form container top margin:
                c.insets = new Insets(32, 0, 0, 0);

                formContainer.add(formTitleLabel, c);


                // title
                c.insets = fieldMargin;
                c.gridy++;
                c.anchor = GridBagConstraints.FIRST_LINE_START;
                formContainer.add(firstNameLabel, c);
                c.insets = labelMargin;
                c.gridy++;
                formContainer.add(firstNameHelperLabel, c);
                c.insets = elementMargin;
                c.gridy++;
                formContainer.add(firstNameTF, c);
                c.gridy++;
                formContainer.add(firstNameErrorLabel, c);

                // runtime
                c.insets = fieldMargin;
                c.gridy++;
                formContainer.add(lastNameLabel, c);
                c.insets = labelMargin;
                c.gridy++;
                formContainer.add(lastNameHelperLabel, c);
                c.insets = elementMargin;
                c.gridy++;
                formContainer.add(lastNameTF, c);
                c.gridy++;
                formContainer.add(lastNameErrorLabel, c);

                // submit button
                c.gridy++;
                c.insets = new Insets(32, 0, 16, 0);
                c.anchor = GridBagConstraints.CENTER;
                formContainer.add(submitBtn, c);
            }

            case IsLoading -> {
                components.keySet().forEach(component -> component.setEnabled(false));
            }
        }
        add(formContainer);
        pack();
    }

    private void wireUpUI() {
        for (var component : components.keySet()) {
            component.addFocusListener(this);


            if (component instanceof JTextComponent textComponent) {
                textComponent.getDocument().addDocumentListener(new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        validate(textComponent);
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        validate(textComponent);
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {

                    }
                });
            }
        }
    }

    private void validate(JComponent component) {
        Attribute attr = components.get(component);

        if (component instanceof JTextComponent textComponent) {

            for (var validator : attr.validators()) {
                if (!validator.predicate().test(textComponent.getText())) {
                    attr.error().setText(validator.message());
                    textComponent.putClientProperty("JComponent.outline", "error");
                    firePropertyChange("checkSubmit", null, null);

                    return;
                }
            }
        } else if (component instanceof JList listComponent) {
            for (var validator : attr.validators()) {
                if (!validator.predicate().test(listComponent.getSelectedValuesList())) {
                    attr.error().setText(validator.message());
                    listComponent.putClientProperty("JComponent.outline", "error");
                    firePropertyChange("checkSubmit", null, null);

                    return;
                }
            }
        }


        firePropertyChange("checkSubmit", null, null);
        component.putClientProperty("JComponent.outline", "");
        attr.error().setText("");
    }

    @Override
    public void focusGained(FocusEvent e) {
        var key = (JComponent) e.getSource();
        validate(key);
    }

    @Override
    public void focusLost(FocusEvent e) {

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Objects.equals(evt.getPropertyName(), "checkSubmit")) {

            boolean isValid = true;

            for (var entry : components.entrySet()) {
                var component = entry.getKey();
                var attr = entry.getValue();

                if (component instanceof JTextComponent textComponent) {
                    for (var validator : attr.validators()) {
                        if (!validator.predicate().test(textComponent.getText())) {
                            isValid = false;
                            submitBtn.setEnabled(false);
                            break;
                        }
                    }
                } else if (component instanceof JList listComponent) {
                    for (var validator : attr.validators()) {
                        if (!validator.predicate().test(listComponent.getSelectedValuesList())) {
                            isValid = false;
                            submitBtn.setEnabled(false);

                            break;
                        }
                    }
                }


            }

            if (isValid)
                submitBtn.setEnabled(true);
        }
    }
}
