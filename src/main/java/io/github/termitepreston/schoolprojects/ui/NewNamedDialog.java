package io.github.termitepreston.schoolprojects.ui;

import io.github.termitepreston.schoolprojects.DB;
import io.github.termitepreston.schoolprojects.model.Named;

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

public class NewNamedDialog extends JDialog implements FocusListener, PropertyChangeListener {
    private final DB db;
    private final Status dataInsertStatus = Status.Idle;
    private final Insets fieldMargin = new Insets(20, 0, 0, 0);
    private final Insets labelMargin = new Insets(8, 0, 0, 0);
    private final Insets elementMargin = new Insets(4, 0, 0, 0);
    private final JLabel formTitleLabel = new JLabel();

    private final JLabel nameLabel = new JLabel();
    private final JLabel nameHelperLabel = new JLabel();
    private final JTextField nameTF;
    private final JLabel nameErrorLabel = new JLabel("");
    private final JButton submitBtn = new JButton();
    private final JPanel formContainer = new JPanel();
    private final HashMap<JComponent, Attribute> components = new HashMap<>();
    private final BiFunction<Class<? extends Named>, String, SwingWorker<Named, Void>> namedInserter;
    private final Class<? extends Named> clazz;
    private Named named;

    public NewNamedDialog(DB db, Class<? extends Named> clazz, int cols) {
        this.db = db;
        this.clazz = clazz;
        nameTF = new JTextField(cols);
        setPreferredSize(new Dimension(384, 360));

        formTitleLabel.setText("New %s".formatted(clazz.getSimpleName()));
        submitBtn.setText("Add %s...".formatted(clazz.getSimpleName().toLowerCase()));
        nameLabel.setText("%s's name".formatted(clazz.getSimpleName()));
        nameHelperLabel.setText("Name of the %s...".formatted(clazz.getSimpleName().toLowerCase()));

        // set the style of title labels.
        JLabel[] labels = {
                nameLabel,
        };
        Arrays.stream(labels).forEach(l -> l.putClientProperty("FlatLaf.style", "font: $h2.regular.font"));

        // set the style of helpers
        JLabel[] helpers = {
                nameHelperLabel,
        };
        Arrays.stream(helpers).forEach(h -> h.putClientProperty("FlatLaf.style", "font: $medium.font"));

        // set the style of error labels.
        JLabel[] errors = {
                nameErrorLabel,
        };
        Arrays.stream(errors).forEach(e -> {
            e.putClientProperty("FlatLaf.style", "font: $defaultFont; foreground: #d93b3b");
        });

        formTitleLabel.putClientProperty("FlatLaf.style", "font: $h1.font");

        namedInserter = (c, name) -> new SwingWorker<>() {
            @Override
            protected Named doInBackground() throws Exception {
                return Named.insertOne(NewNamedDialog.this.db, c, name);
            }

            @Override
            protected void done() {
                try {

                    setNamed(get());

                    JOptionPane.showMessageDialog(NewNamedDialog.this,
                            "Added %s to database successfully!".formatted(named));
                    dispose();

                } catch (ExecutionException | InterruptedException e) {
                    setNamed(null);

                    JOptionPane.showMessageDialog(NewNamedDialog.this,
                            e.getCause().getMessage(),
                            "Error!",
                            JOptionPane.ERROR_MESSAGE);
                    dispose();
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

        components.put(nameTF, new Attribute(new Validator[]{required.apply("Last name")}, nameLabel, nameHelperLabel, nameErrorLabel));

        submitBtn.setEnabled(false);

        setTitle("New %s...".formatted(clazz.getSimpleName()));
        setModal(true);

        addPropertyChangeListener(this);

        formContainer.setLayout(new GridBagLayout());

        buildUI();
        wireUpUI();
    }

    public static Named createNamed(DB db, Class<? extends Named> clazz) {
        NewNamedDialog nnd = new NewNamedDialog(db, clazz, 20);

        nnd.setVisible(true);

        return nnd.getNamed();
    }

    public Named getNamed() {
        return named;
    }

    public void setNamed(Named named) {
        this.named = named;
    }

    private void buildUI() {
        GridBagConstraints c = new GridBagConstraints();

        switch (dataInsertStatus) {
            case Idle -> {
                c.gridx = 0;
                c.gridy = 0;
                c.anchor = GridBagConstraints.FIRST_LINE_START;

                // form container top margin:
                c.insets = new Insets(32, 0, 0, 0);

                formContainer.add(formTitleLabel, c);


                // name
                c.insets = fieldMargin;
                c.gridy++;
                formContainer.add(nameLabel, c);
                c.insets = labelMargin;
                c.gridy++;
                formContainer.add(nameHelperLabel, c);
                c.insets = elementMargin;
                c.gridy++;
                formContainer.add(nameTF, c);
                c.gridy++;
                formContainer.add(nameErrorLabel, c);

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

        submitBtn.addActionListener(e -> {
            var inserter = namedInserter.apply(clazz, nameTF.getText());

            inserter.execute();
        });
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
