package io.github.termitepreston.schoolprojects.ui;

import io.github.termitepreston.schoolprojects.DB;
import io.github.termitepreston.schoolprojects.model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class ApplicationFrame extends JFrame implements PropertyChangeListener, ActionListener {
    private final DB db;
    private final JLabel welcomeLabel = new JLabel();
    private final SwingWorker<HashMap<Class<?>, ArrayList<?>>, Void> tablesFetcher;

    private final HashMap<Class<?>, DefaultTableModel> tableModels;
    private final Status tablesFetchStatus = Status.IsLoading;
    private final User user;
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

    public ApplicationFrame(String title, DB db) {
        super(title);

        this.db = db;

        tableModels = new HashMap<>();
        tablesFetcher = new SwingWorker<>() {
            @Override
            protected HashMap<Class<?>, ArrayList<?>> doInBackground() throws Exception {
                var models = new HashMap<Class<?>, ArrayList<?>>();

                // fetch all for each model.
                var movies = Movie.getAll(db);

                var actors = Person.getAll(db, Actor.class);

                var directors = Person.getAll(db, Director.class);

                var genres = Named.getAll(db, Genre.class);

                var languages = Named.getAll(db, Language.class);

                var ratings = Named.getAll(db, RatingsAgency.class);

                models.put(Movie.class, movies);
                models.put(Actor.class, actors);
                models.put(Director.class, directors);
                models.put(Genre.class, genres);
                models.put(Language.class, languages);
                models.put(RatingsAgency.class, ratings);

                return models;
            }

            @Override
            protected void done() {
                try {
                    addPropertyChangeListener(ApplicationFrame.this);

                    var models = get();

                    for (var entry : models.entrySet()) {
                        var clazz = entry.getKey();
                        var list = entry.getValue();


                        if (clazz == Actor.class || clazz == Director.class) {
                            tableModels.put(clazz, new DefaultTableModel(
                                    list.stream().map(o -> {
                                        var person = (Person) o;

                                        return new Object[]{
                                                person.getId(),
                                                person.getFirstName(),
                                                person.getLastName()
                                        };
                                    }).toArray(Object[][]::new),
                                    new String[]{
                                            "ID", "First Name", "Last Name"
                                    }) {
                                final Class<?>[] columnTypes = new Class<?>[]{
                                        Integer.class,
                                        String.class,
                                        String.class,
                                };

                                final boolean[] columnEditable = {
                                        false, false, false
                                };

                                @Override
                                public Class<?> getColumnClass(int columnIndex) {
                                    return columnTypes[columnIndex];
                                }

                                @Override
                                public boolean isCellEditable(int rowIndex, int columnIndex) {
                                    return columnEditable[columnIndex];
                                }
                            });
                        } else if (clazz == Genre.class || clazz == Language.class || clazz == RatingsAgency.class) {
                            tableModels.put(clazz, new DefaultTableModel(
                                    list.stream().map(o -> {
                                        var named = (Named) o;

                                        return new Object[]{
                                                named.getId(),
                                                named.getName(),
                                        };
                                    }).toArray(Object[][]::new),
                                    new String[]{
                                            "ID", "Name"
                                    }) {
                                final Class<?>[] columnTypes = new Class<?>[]{
                                        Integer.class,
                                        String.class,
                                };

                                final boolean[] columnEditable = {
                                        false, false
                                };

                                @Override
                                public Class<?> getColumnClass(int columnIndex) {
                                    return columnTypes[columnIndex];
                                }

                                @Override
                                public boolean isCellEditable(int rowIndex, int columnIndex) {
                                    return columnEditable[columnIndex];
                                }
                            });
                        } else if (clazz == Movie.class) {
                            tableModels.put(clazz, new DefaultTableModel(
                                    list.stream().map(o -> {
                                        var movie = (Movie) o;

                                        return new Object[]{
                                                movie.getId(),
                                                movie.getTitle(),
                                                movie.getYear(),
                                                movie.getRuntime(),
                                                movie.getPlot(),
                                                movie.getAwards(),
                                                movie.getBoxOffice(),
                                                movie.getActors().size() > 1 ? movie.getActors().get(0) + " and others..." : movie.getActors().get(0),
                                                movie.getDirectors().size() > 1 ? movie.getDirectors().get(0) + " and others..." : movie.getDirectors().get(0),
                                                movie.getGenres().size() > 1 ? movie.getGenres().get(0) + "and others..." : movie.getGenres().get(0),
                                                movie.getLanguages().size() > 1 ? movie.getLanguages().get(0) + "and others..." : movie.getLanguages().get(0),
                                                "IMDB: 30"
                                        };
                                    }).toArray(Object[][]::new), new String[]{
                                    "ID",
                                    "Title",
                                    "Year",
                                    "Runtime",
                                    "Plot",
                                    "Awards",
                                    "Box Office",
                                    "Actors",
                                    "Directors",
                                    "Genres"
                            }) {
                                final Class<?>[] columnTypes = new Class<?>[]{
                                        Integer.class,
                                        String.class,
                                        Integer.class,
                                        Integer.class,
                                        String.class,
                                        String.class,
                                        Double.class,
                                        String.class,
                                        String.class,
                                        String.class
                                };

                                final boolean[] columnEditable = {
                                        false, false, false, false, false, false, false, false, false, false
                                };

                                @Override
                                public Class<?> getColumnClass(int columnIndex) {
                                    return columnTypes[columnIndex];
                                }

                                @Override
                                public boolean isCellEditable(int rowIndex, int columnIndex) {
                                    return columnEditable[columnIndex];
                                }
                            });
                        }
                    }

                    firePropertyChange("tablesFetchStatus",
                            tablesFetchStatus,
                            Status.HasSucceeded);
                } catch (InterruptedException | ExecutionException e) {
                    JOptionPane.showMessageDialog(ApplicationFrame.this,
                            e.getCause().getMessage(), "Fatal Error!", JOptionPane.ERROR_MESSAGE);
                    ApplicationFrame.this.dispose();
                }
            }
        };

        tablesFetcher.execute();

        addPropertyChangeListener(this);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setMinimumSize(new Dimension(960, 768));

        user = AuthDialog.login(this, db);

        buildUI();

        setVisible(true);
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
        // common UI
        buildCommonMenuBar();
        addQuitMenuItem();

        if (user == null || !user.isAdmin()) { // regular user...
            // no tab pane just browse movies...
            JTable userTable = new JTable(tableModels.get(Movie.class));

            add(new JScrollPane(userTable), BorderLayout.CENTER);

            pack();

            return;
        }

        // admin UI
        JTabbedPane pane = new JTabbedPane();

        JTable moviesTableAdmin = new JTable(tableModels.get(Movie.class));
        JPopupMenu popup = new JPopupMenu();

        JMenuItem update = new JMenuItem("Update");
        JMenuItem delete = new JMenuItem("Delete");

        update.addActionListener(l -> {
            int selectedRow = moviesTableAdmin.getSelectedRow();

            var model = tableModels.get(Movie.class);

            var id = (int) model.getDataVector().get(selectedRow).get(0);

            Movie movie = MovieDialog.updateMovie(db, 24, 8, id);

            if (movie != null) {
                model.removeRow(selectedRow);
                tableModels.get(Movie.class).addRow(new Object[]{
                        movie.getId(),
                        movie.getTitle(),
                        movie.getYear(),
                        movie.getRuntime(),
                        movie.getPlot(),
                        movie.getAwards(),
                        movie.getBoxOffice(),
                        movie.getActors().size() > 1 ? movie.getActors().get(0) + " and others..." : movie.getActors().get(0),
                        movie.getDirectors().size() > 1 ? movie.getDirectors().get(0) + " and others..." : movie.getDirectors().get(0),
                        movie.getGenres().size() > 1 ? movie.getGenres().get(0) + "and others..." : movie.getGenres().get(0),
                        movie.getLanguages().size() > 1 ? movie.getLanguages().get(0) + "and others..." : movie.getLanguages().get(0),
                        "IMDB: 30"
                });
            }

            System.out.println("id = " + id);
        });

        delete.addActionListener(l -> {
            int selectedRow = moviesTableAdmin.getSelectedRow();

            var model = tableModels.get(Movie.class);

            var id = (int) model.getDataVector().get(selectedRow).get(0);

            boolean success = MovieDialog.deleteMovie(db, id);

            if (success) {
                model.removeRow(selectedRow);
            }
        });


        popup.add(update);
        popup.add(delete);

        moviesTableAdmin.setComponentPopupMenu(popup);

        JTable actorsTable = new JTable(tableModels.get(Actor.class));
        JTable directorsTable = new JTable(tableModels.get(Director.class));
        JTable genresTable = new JTable(tableModels.get(Genre.class));
        JTable languagesTable = new JTable(tableModels.get(Language.class));
        JTable ratingsTable = new JTable(tableModels.get(RatingsAgency.class));


        pane.add("Movies", new JScrollPane(moviesTableAdmin));
        pane.add("Actors", new JScrollPane(actorsTable));
        pane.add("Directors", new JScrollPane(directorsTable));
        pane.add("Genres", new JScrollPane(genresTable));
        pane.add("Languages", new JScrollPane(languagesTable));
        pane.add("Ratings Agency", new JScrollPane(ratingsTable));

        add(pane, BorderLayout.CENTER);

        buildAdminMenuBar();

        pack();
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
            var movie = MovieDialog.createMovie(db, 8, 24);

            if (movie != null)
                tableModels.get(Movie.class).addRow(new Object[]{
                        movie.getId(),
                        movie.getTitle(),
                        movie.getYear(),
                        movie.getRuntime(),
                        movie.getPlot(),
                        movie.getAwards(),
                        movie.getBoxOffice(),
                        movie.getActors().size() > 1 ? movie.getActors().get(0) + " and others..." : movie.getActors().get(0),
                        movie.getDirectors().size() > 1 ? movie.getDirectors().get(0) + " and others..." : movie.getDirectors().get(0),
                        movie.getGenres().size() > 1 ? movie.getGenres().get(0) + "and others..." : movie.getGenres().get(0),
                        movie.getLanguages().size() > 1 ? movie.getLanguages().get(0) + "and others..." : movie.getLanguages().get(0),
                        "IMDB: 30"
                });
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }
}
