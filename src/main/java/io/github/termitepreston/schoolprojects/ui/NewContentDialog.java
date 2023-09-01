package io.github.termitepreston.schoolprojects.ui;

import io.github.termitepreston.schoolprojects.DB;
import io.github.termitepreston.schoolprojects.Pair;
import io.github.termitepreston.schoolprojects.model.ContentTypes;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public class NewContentDialog extends JDialog implements PropertyChangeListener, ItemListener {
    private final DB db;
    private final JProgressBar initialLoadPB;
    private final MigLayout formLayout;
    private final JComboBox<String> contentTypesCB;
    private final JLabel typeLabel = new JLabel("Type");
    private final JLabel titleLabel = new JLabel("Title");

    private final JTextField titleTF = new JTextField();
    private final JLabel plotLabel = new JLabel("Plot");
    private final JTextArea plotTA = new JTextArea(6, 40);

    private final JLabel dateLabel = new JLabel("Date");

    private final JFormattedTextField dateFTF = new JFormattedTextField(new SimpleDateFormat("MM/dd/yy"));

    private final JLabel awardsLabel = new JLabel("Awards");

    private final JTextArea awardsTA = new JTextArea(6, 40);

    // series specific panel
    private final JLabel boxOfficeLabel = new JLabel("Box Office");

    private final JTextField boxOfficeTF = new JTextField();

    private final JLabel episodeL = new JLabel("Episode");
    private final JSpinner episodeS = new JSpinner();

    private final JLabel seasonL = new JLabel("Season");

    private final JSpinner seasonS = new JSpinner();

    private final JLabel seriesL = new JLabel("Series");

    private final JComboBox<String> seriesCB = new JComboBox<>();

    private final JLabel writerL = new JLabel("Writer");

    private final JTextField writerTF = new JTextField();

    // private final MultiInputField actorsMIF = new MultiInputField("Add actor...");

    private final JLabel actorsL = new JLabel("Actors");

    private final HashMap<String, Pair<ArrayList<Container>, Consumer<Container>>> formRenderers = new HashMap<>();

    private ContentTypes contentTypes;
    private boolean initialRender = false;

    public NewContentDialog(DB db) {
        this.db = db;

        addPropertyChangeListener(this);

        initialLoadPB = new JProgressBar();
        contentTypesCB = new JComboBox<>();
        formLayout = new MigLayout();

        setModal(true);
        setMinimumSize(new Dimension(320, 320));
        getContentPane().setLayout(formLayout);

        // dynamic form fields

        formRenderers.put("Movie", new Pair<>(
                new ArrayList<>(List.of(boxOfficeLabel, boxOfficeTF, actorsL, null)),
                (pane) -> {
                    pane.add(boxOfficeLabel);
                    pane.add(boxOfficeTF, "wrap");

                    pane.add(actorsL);
                    // pane.add(actorsMIF, "wrap");
                }
        ));

        formRenderers.put("Series", new Pair<>(
                new ArrayList<>(List.of(writerL, writerTF)),
                (pane) -> {
                    pane.add(writerL);
                    pane.add(writerTF, "wrap");
                }
        ));

        formRenderers.put("Episode", new Pair<>(
                new ArrayList<>(List.of(seriesL, seriesCB, seasonL, seasonS, episodeL, episodeS)),
                (pane) -> {
                    pane.add(seriesL);
                    pane.add(seriesCB, "wrap");

                    pane.add(seasonL);
                    pane.add(seasonS, "wrap");

                    pane.add(episodeL);
                    pane.add(episodeS, "wrap");
                }
        ));


        loadingUI();
    }

    public ContentTypes getContentTypes() {
        return contentTypes;
    }

    public void setContentTypes(ContentTypes contentTypes) {
        var oldContentTypes = this.contentTypes;
        this.contentTypes = contentTypes;

        firePropertyChange("contentTypes", oldContentTypes, contentTypes);
    }

    private void loadContentTypes() {
        var contentTypesWorker = new SwingWorker<ContentTypes, Void>() {

            @Override
            protected ContentTypes doInBackground() throws Exception {
                ContentTypes ct = new ContentTypes(db);

                ct.loadContentTypes();

                return ct;
            }

            @Override
            protected void done() {
                try {
                    var ct = get();

                    setContentTypes(ct);
                } catch (ExecutionException e) {

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        contentTypesWorker.execute();
    }

    private void loadingUI() {
        var pane = getContentPane();

        initialLoadPB.setIndeterminate(true);
        pane.add(initialLoadPB, "grow");

        loadContentTypes();
    }

    private void defaultUI() {
        var pane = getContentPane();

        pane.remove(initialLoadPB);

        // should always be called after a removal of components.
        // option 1.
        revalidate();
        repaint();

        var layout = new MigLayout();

        formLayout.setLayoutConstraints("inset 16");
        formLayout.setColumnConstraints("[48!]16[]");

        contentTypesCB.addItemListener(this);
        contentTypes.getContentTypes().forEach((k, v) -> {
            contentTypesCB.addItem(v);
        });
        contentTypesCB.setSelectedIndex(0);

        pane.add(typeLabel);
        pane.add(contentTypesCB, "wrap");

        pane.add(titleLabel);
        pane.add(titleTF, "wrap, grow");

        pane.add(plotLabel);
        pane.add(plotTA, "wrap");

        pane.add(dateLabel);
        pane.add(dateFTF, "wrap");

        pane.add(awardsLabel);
        pane.add(awardsTA, "wrap");

        // render ui for either of series, episode, movie
        var selectItem = (String) contentTypesCB.getSelectedItem();
        var renderer = formRenderers.get(selectItem);

        initialRender = true;

        renderer.getSecond().accept(pane);
    }

    private void renderFormFragment(Object item) {
        var pane = getContentPane();

        var renderer = formRenderers.get((String) item);

        renderer.getSecond().accept(pane);

        pack();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("contentTypes")) {
            if (evt.getOldValue() == null && evt.getNewValue() != null)
                defaultUI();
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == contentTypesCB) {
            if (e.getStateChange() == ItemEvent.SELECTED && initialRender) {
                renderFormFragment(e.getItem());
            }

            if (e.getStateChange() == ItemEvent.DESELECTED) {
                removeFormFragment((String) e.getItem());
            }
        }
    }

    private void removeFormFragment(String item) {
        var components = formRenderers.get(item).getFirst();

        for (var comp : components)
            getContentPane().remove(comp);

        getContentPane().revalidate();
        getContentPane().repaint();
    }
}
