package io.github.termitepreston.schoolprojects.ui;

import io.github.termitepreston.schoolprojects.DB;
import io.github.termitepreston.schoolprojects.model.*;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;

public class NewMovieDialog extends JDialog implements FocusListener, ItemListener, PropertyChangeListener, ListSelectionListener {
    private final static String PROPERTY_SEED_DATA_LOAD_SUCCESS = "seedDataLoadSuccess";
    private final Insets fieldMargin = new Insets(20, 0, 0, 0);
    private final Insets labelMargin = new Insets(8, 0, 0, 0);
    private final Insets elementMargin = new Insets(4, 0, 0, 0);
    private final JLabel formTitleLabel = new JLabel("New Movie");
    private final JLabel titleLabel = new JLabel("Title");
    private final JLabel titleHelperLabel = new JLabel("Title of the movie...");
    private final JTextField titleTF;
    private final JLabel titleErrorLabel = new JLabel("");
    private final JLabel runtimeLabel = new JLabel("Runtime");
    private final JLabel runtimeHelperLabel = new JLabel("Runtime of the movie...");
    private final JTextField runtimeTF;
    private final JLabel runtimeErrorLabel = new JLabel("");
    private final JLabel yearLabel = new JLabel("Year");
    private final JLabel yearHelperLabel = new JLabel("The year the movie was released.");
    private final JTextField yearTF;
    private final JLabel yearErrorLabel = new JLabel("");
    private final JLabel boxOfficeLabel = new JLabel("Box office");
    private final JLabel boxOfficeHelperLabel = new JLabel("Box office earned...");
    private final JTextField boxOfficeTF;
    private final JLabel boxOfficeErrorLabel = new JLabel("");
    private final JLabel actorsLabel = new JLabel("Actors");
    private final JLabel actorsHelperLabel = new JLabel("Choose one or more actor...");
    private final JList<Person> actorsList = new JList<>();
    private final JLabel actorsErrorLabel = new JLabel("");
    private final JButton addActorBtn = new JButton("Add Actor...");
    private final JLabel genresLabel = new JLabel("Genre");
    private final JLabel genresHelperLabel = new JLabel("Genre of the movie...");
    private final JList<Named> genresList = new JList<>();
    private final JLabel genresErrorLabel = new JLabel("");
    private final JButton addGenreBtn = new JButton("Add Genre...");


    private final JLabel directorsLabel = new JLabel("Directors");
    private final JLabel directorsHelperLabel = new JLabel("Choose one or more director");
    private final JList<Person> directorsList = new JList<>();
    private final JLabel directorsErrorLabel = new JLabel("");
    private final JButton addDirectorBtn = new JButton("Add Director...");

    private final JLabel languagesLabel = new JLabel("Languages");
    private final JLabel languagesHelperLabel = new JLabel("Choose one or more language");
    private final JList<Named> languagesList = new JList<>();
    private final JLabel languagesErrorLabel = new JLabel("");
    private final JButton addLanguageBtn = new JButton("Add Language...");

    private final JLabel ratingsLabel = new JLabel("Ratings");
    private final JLabel ratingsHelperLabel = new JLabel("Set one or more ratings...");
    private final JList<Named> ratingsList = new JList<>();
    private final JLabel ratingsErrorLabel = new JLabel("");
    private final JButton addRatingsBtn = new JButton("Add Ratings...");

    private final JLabel awardsLabel = new JLabel("Awards");
    private final JLabel awardsHelperLabel = new JLabel("Awards won by the movie...");
    private final JTextArea awardsTA;
    private final JLabel awardsErrorLabel = new JLabel("");
    private final JLabel plotLabel = new JLabel("Plot");
    private final JLabel plotHelperLabel = new JLabel("Movie's plot...");
    private final JTextArea plotTA;
    private final JLabel plotErrorLabel = new JLabel("");
    private final HashMap<JComponent, Attribute> components = new HashMap<>();
    private final JButton submitBtn = new JButton("Add Movie...");
    private final SwingWorker[] fetchers;
    private final JPanel formContainer = new JPanel();
    private final JLabel loadingLabel = new JLabel("Loading...");
    private Status seedDataLoadStatus = Status.Idle;
    private int seedDataLoadSuccess = 0;

    public NewMovieDialog(String title, DB db, int rows, int cols) {
        // construct text fields and text areas...

        seedDataLoadStatus = Status.IsLoading;

        BiFunction<Class<? extends Person>, JList<Person>, SwingWorker<ArrayList<Person>, Void>> personsFetcherFactory = (clazz, list) -> new SwingWorker<>() {
            @Override
            protected ArrayList<Person> doInBackground() throws Exception {
                return Person.getAll(db, clazz);
            }

            @Override
            protected void done() {
                try {
                    addPropertyChangeListener(NewMovieDialog.this);

                    var people = get();

                    var listModel = new DefaultListModel<Person>();

                    listModel.addAll(people);

                    list.setModel(listModel);

                    firePropertyChange(PROPERTY_SEED_DATA_LOAD_SUCCESS, seedDataLoadSuccess, seedDataLoadSuccess + 1);

                } catch (ExecutionException e) {
                    firePropertyChange("seedDataLoadStatus", seedDataLoadStatus, Status.HasError);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        BiFunction<Class<? extends Named>, JList<Named>, SwingWorker<ArrayList<Named>, Void>> namedOnesFetcherFactory = (clazz, list) -> new SwingWorker<>() {
            @Override
            protected ArrayList<Named> doInBackground() throws Exception {
                return Named.getAll(db, clazz);
            }

            @Override
            protected void done() {
                try {
                    addPropertyChangeListener(NewMovieDialog.this);

                    var namedOnes = get();

                    var listModel = new DefaultListModel<Named>();

                    listModel.addAll(namedOnes);

                    list.setModel(listModel);

                    firePropertyChange(PROPERTY_SEED_DATA_LOAD_SUCCESS, seedDataLoadSuccess, seedDataLoadSuccess + 1);

                } catch (ExecutionException e) {
                    firePropertyChange("seedDataLoadStatus", seedDataLoadStatus, Status.HasError);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };


        fetchers = new SwingWorker[]{
                personsFetcherFactory.apply(Actor.class, actorsList),
                personsFetcherFactory.apply(Director.class, directorsList),
                namedOnesFetcherFactory.apply(Genre.class, genresList),
                namedOnesFetcherFactory.apply(Language.class, languagesList),
                namedOnesFetcherFactory.apply(RatingsAgency.class, ratingsList)
        };

        for (var fetcher : fetchers)
            fetcher.execute();

        titleTF = new JTextField(cols);
        yearTF = new JTextField(cols);
        boxOfficeTF = new JTextField(cols);
        runtimeTF = new JTextField(cols);
        awardsTA = new JTextArea(rows, cols);
        plotTA = new JTextArea(rows, cols);

        // set the style of title labels.
        JLabel[] labels = {
                titleLabel,
                runtimeLabel,
                yearLabel,
                boxOfficeLabel,
                awardsLabel,
                plotLabel,
                genresLabel,
                actorsLabel,
                directorsLabel,
                languagesLabel,
                ratingsLabel
        };
        Arrays.stream(labels).forEach(l -> l.putClientProperty("FlatLaf.style", "font: $h2.regular.font"));

        // set the style of helpers
        JLabel[] helpers = {
                titleHelperLabel,
                runtimeHelperLabel,
                yearHelperLabel,
                boxOfficeHelperLabel,
                awardsHelperLabel,
                plotHelperLabel,
                genresHelperLabel,
                actorsHelperLabel,
                directorsHelperLabel,
                languagesHelperLabel,
                ratingsHelperLabel
        };
        Arrays.stream(helpers).forEach(h -> h.putClientProperty("FlatLaf.style", "font: $medium.font"));

        // set the style of error labels.
        JLabel[] errors = {
                titleErrorLabel,
                runtimeErrorLabel,
                yearErrorLabel,
                boxOfficeErrorLabel,
                awardsErrorLabel,
                plotErrorLabel,
                actorsErrorLabel,
                directorsErrorLabel,
                genresErrorLabel,
                languagesErrorLabel,
                ratingsErrorLabel
        };
        Arrays.stream(errors).forEach(e -> {
            e.putClientProperty("FlatLaf.style", "font: $defaultFont; foreground: #d93b3b");
        });

        formTitleLabel.putClientProperty("FlatLaf.style", "font: $h1.font");
        loadingLabel.putClientProperty("FlatLaf.style", "font: $h1.font");

        Function<String, Validator> required = prefix -> new Validator(o -> {
            if (o instanceof String[] arr)
                return arr.length > 0;

            if (o instanceof java.util.List list)
                return !list.isEmpty();

            if (o instanceof String s)
                return !s.isEmpty();

            return false;
        }, prefix + " is required!");


        components.put(titleTF, new Attribute(new Validator[]{required.apply("Title")}, titleLabel, titleHelperLabel, titleErrorLabel));
        components.put(runtimeTF, new Attribute(new Validator[]{required.apply("Runtime")}, runtimeLabel, runtimeHelperLabel, runtimeErrorLabel));
        components.put(yearTF, new Attribute(new Validator[]{required.apply("Year")}, yearLabel, yearHelperLabel, yearErrorLabel));
        components.put(boxOfficeTF, new Attribute(new Validator[]{required.apply("Box office")}, boxOfficeLabel, boxOfficeHelperLabel, boxOfficeErrorLabel));
        components.put(plotTA, new Attribute(new Validator[]{required.apply("Plot")}, plotLabel, plotHelperLabel, plotErrorLabel));
        components.put(awardsTA, new Attribute(new Validator[]{required.apply("Awards")}, awardsLabel, awardsHelperLabel, awardsErrorLabel));
        components.put(actorsList, new Attribute(new Validator[]{required.apply("Actor")}, actorsLabel, actorsHelperLabel, actorsErrorLabel));
        components.put(directorsList, new Attribute(new Validator[]{required.apply("Director")}, directorsLabel, directorsHelperLabel, directorsErrorLabel));
        components.put(genresList, new Attribute(new Validator[]{required.apply("Genre")}, genresLabel, genresHelperLabel, genresErrorLabel));
        components.put(languagesList, new Attribute(new Validator[]{required.apply("Language")}, languagesLabel, languagesHelperLabel, languagesErrorLabel));
        components.put(ratingsList, new Attribute(new Validator[]{required.apply("Ratings")}, ratingsLabel, ratingsHelperLabel, ratingsErrorLabel));

        addActorBtn.addActionListener(e -> {
            var person = NewPersonDialog.createPerson(db, Actor.class);
            System.out.println("person = " + person);
        });

        submitBtn.setEnabled(false);

        setTitle(title);
        setModal(true);
        setPreferredSize(new Dimension(384, 600));

        addPropertyChangeListener(this);

        formContainer.setLayout(new GridBagLayout());


        buildUI();
        wireUpUI();
    }

    private void buildUI() {
        GridBagConstraints c = new GridBagConstraints();


        switch (seedDataLoadStatus) {
            case IsLoading -> {
                c.gridx = 0;
                c.gridy = 0;
                c.anchor = GridBagConstraints.CENTER;

                formContainer.add(loadingLabel, c);
                add(formContainer);
            }

            case HasSucceeded -> {
                // first remove all ui components from loading ui state.
                remove(formContainer);
                formContainer.removeAll();
                revalidate();
                repaint();

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
                formContainer.add(titleLabel, c);
                c.insets = labelMargin;
                c.gridy++;
                formContainer.add(titleHelperLabel, c);
                c.insets = elementMargin;
                c.gridy++;
                formContainer.add(titleTF, c);
                c.gridy++;
                formContainer.add(titleErrorLabel, c);

                // runtime
                c.insets = fieldMargin;
                c.gridy++;
                formContainer.add(runtimeLabel, c);
                c.insets = labelMargin;
                c.gridy++;
                formContainer.add(runtimeHelperLabel, c);
                c.insets = elementMargin;
                c.gridy++;
                formContainer.add(runtimeTF, c);
                c.gridy++;
                formContainer.add(runtimeErrorLabel, c);

                // year
                c.gridy++;
                c.insets = fieldMargin;
                formContainer.add(yearLabel, c);
                c.insets = labelMargin;
                c.gridy++;
                formContainer.add(yearHelperLabel, c);
                c.insets = elementMargin;
                c.gridy++;
                formContainer.add(yearTF, c);
                c.gridy++;
                formContainer.add(yearErrorLabel, c);

                // box office
                c.gridy++;
                c.insets = fieldMargin;
                formContainer.add(boxOfficeLabel, c);
                c.insets = labelMargin;
                c.gridy++;
                formContainer.add(boxOfficeHelperLabel, c);
                c.insets = elementMargin;
                c.gridy++;
                formContainer.add(boxOfficeTF, c);
                c.gridy++;
                formContainer.add(boxOfficeErrorLabel, c);

                // actor
                c.gridy++;
                c.insets = fieldMargin;
                formContainer.add(actorsLabel, c);
                c.insets = labelMargin;
                c.gridy++;
                formContainer.add(actorsHelperLabel, c);
                c.insets = elementMargin;
                c.gridy++;
                c.fill = GridBagConstraints.HORIZONTAL;
                formContainer.add(new JScrollPane(actorsList), c);
                // reset fill
                c.fill = GridBagConstraints.NONE;
                c.gridy++;
                formContainer.add(actorsErrorLabel, c);
                c.gridy++;
                c.anchor = GridBagConstraints.LAST_LINE_END;
                formContainer.add(addActorBtn, c);
                // reset anchor
                c.anchor = GridBagConstraints.FIRST_LINE_START;

                // directors
                c.gridy++;
                c.insets = fieldMargin;
                formContainer.add(directorsLabel, c);
                c.insets = labelMargin;
                c.gridy++;
                formContainer.add(directorsHelperLabel, c);
                c.insets = elementMargin;
                c.gridy++;
                c.fill = GridBagConstraints.HORIZONTAL;
                formContainer.add(new JScrollPane(directorsList), c);
                // reset fill
                c.fill = GridBagConstraints.NONE;
                c.gridy++;
                formContainer.add(directorsErrorLabel, c);
                c.gridy++;
                c.anchor = GridBagConstraints.LAST_LINE_END;
                formContainer.add(addDirectorBtn, c);
                // reset anchor
                c.anchor = GridBagConstraints.FIRST_LINE_START;

                // genre
                c.gridy++;
                c.insets = fieldMargin;
                formContainer.add(genresLabel, c);
                c.insets = labelMargin;
                c.gridy++;
                formContainer.add(genresHelperLabel, c);
                c.insets = elementMargin;
                c.gridy++;
                c.fill = GridBagConstraints.HORIZONTAL;
                formContainer.add(new JScrollPane(genresList), c);
                // reset fill
                c.fill = GridBagConstraints.NONE;
                c.gridy++;
                formContainer.add(genresErrorLabel, c);
                c.gridy++;
                c.anchor = GridBagConstraints.LAST_LINE_END;
                formContainer.add(addGenreBtn, c);
                // reset anchor
                c.anchor = GridBagConstraints.FIRST_LINE_START;

                // language
                c.gridy++;
                c.insets = fieldMargin;
                formContainer.add(languagesLabel, c);
                c.insets = labelMargin;
                c.gridy++;
                formContainer.add(languagesHelperLabel, c);
                c.insets = elementMargin;
                c.gridy++;
                c.fill = GridBagConstraints.HORIZONTAL;
                formContainer.add(new JScrollPane(languagesList), c);
                // reset fill
                c.fill = GridBagConstraints.NONE;
                c.gridy++;
                formContainer.add(languagesErrorLabel, c);
                c.gridy++;
                c.anchor = GridBagConstraints.LAST_LINE_END;
                formContainer.add(addLanguageBtn, c);
                // reset anchor
                c.anchor = GridBagConstraints.FIRST_LINE_START;

                // ratings
                c.gridy++;
                c.insets = fieldMargin;
                formContainer.add(ratingsLabel, c);
                c.insets = labelMargin;
                c.gridy++;
                formContainer.add(ratingsHelperLabel, c);
                c.insets = elementMargin;
                c.gridy++;
                c.fill = GridBagConstraints.HORIZONTAL;
                formContainer.add(new JScrollPane(ratingsList), c);
                // reset fill
                c.fill = GridBagConstraints.NONE;
                c.gridy++;
                formContainer.add(ratingsErrorLabel, c);
                c.gridy++;
                c.anchor = GridBagConstraints.LAST_LINE_END;
                formContainer.add(addRatingsBtn, c);
                // reset anchor
                c.anchor = GridBagConstraints.FIRST_LINE_START;


                // awards
                c.gridy++;
                c.insets = fieldMargin;
                formContainer.add(awardsLabel, c);
                c.insets = labelMargin;
                c.gridy++;
                formContainer.add(awardsHelperLabel, c);
                c.insets = elementMargin;
                c.gridy++;
                formContainer.add(new JScrollPane(awardsTA), c);
                c.gridy++;
                formContainer.add(awardsErrorLabel, c);

                // plot
                c.gridy++;
                c.insets = fieldMargin;
                formContainer.add(plotLabel, c);
                c.insets = labelMargin;
                c.gridy++;
                formContainer.add(plotHelperLabel, c);
                c.gridy++;
                c.insets = elementMargin;
                formContainer.add(new JScrollPane(plotTA), c);
                c.gridy++;
                formContainer.add(plotErrorLabel, c);

                // submit button
                c.gridy++;
                c.insets = new Insets(32, 0, 16, 0);
                c.anchor = GridBagConstraints.CENTER;
                formContainer.add(submitBtn, c);

                var scroller = new JScrollPane(formContainer);
                add(scroller);
            }
        }

        pack();
    }

    private void wireUpUI() {
        for (var component : components.keySet()) {
            component.addFocusListener(this);


            if (component instanceof JList listComponent) {
                listComponent.addListSelectionListener(this);
            } else if (component instanceof JTextComponent textComponent) {
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
    public void itemStateChanged(ItemEvent e) {

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Objects.equals(evt.getPropertyName(), "seedDataLoadStatus")) {
            if (evt.getNewValue() == Status.HasError) {
                JOptionPane.showMessageDialog(this,
                        "Fatal error when trying to load data from database.",
                        "Error!",
                        JOptionPane.ERROR_MESSAGE);
                dispose();
            } else if (evt.getNewValue() == Status.HasSucceeded) {
                seedDataLoadStatus = Status.HasSucceeded;
                buildUI();
            }
        }

        if (Objects.equals(evt.getPropertyName(), PROPERTY_SEED_DATA_LOAD_SUCCESS)) {
            seedDataLoadSuccess++;

            if (seedDataLoadSuccess >= fetchers.length) {
                firePropertyChange("seedDataLoadStatus", seedDataLoadStatus, Status.HasSucceeded);
            }
        }

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

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            validate((JComponent) e.getSource());
        }
    }
}
