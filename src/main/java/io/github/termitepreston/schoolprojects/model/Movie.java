package io.github.termitepreston.schoolprojects.model;

import io.github.termitepreston.schoolprojects.DB;

import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Movie {
    private String title;
    private int year;
    private int runtime;
    private String plot;
    private String awards;
    private String type = "Movie";
    private double boxOffice;
    private List<Person> actors;
    private List<Person> directors;
    private List<Named> genres;
    private List<Named> languages;
    private Map<Named, Float> ratings;
    private int id;

    public Movie(String title, int year, int runtime, String plot, String awards, double boxOffice, List<Person> actors, List<Person> directors, List<Named> genres, List<Named> languages, Map<Named, Float> ratings) {
        this.title = title;
        this.year = year;
        this.runtime = runtime;
        this.plot = plot;
        this.awards = awards;
        this.boxOffice = boxOffice;
        this.actors = actors;
        this.directors = directors;
        this.genres = genres;
        this.languages = languages;
        this.ratings = ratings;
    }

    public Movie() {
        actors = new ArrayList<>();
        directors = new ArrayList<>();
        genres = new ArrayList<>();
        languages = new ArrayList<>();
        ratings = new HashMap<>();
    }

    public static Movie getOne(DB db, int mid) throws SQLException {
        String selectOne = """
                select
                    m.id, m.title, m.year, m.runtime, m.plot, m.awards, t.val, m.box_office
                from
                    movie as m
                inner join
                    type as t
                    on m.tid = t.id
                where
                    m.id = ?""";


        Function<Class<?>, String> personSelectAll = (c) -> """
                select
                    r.id, r.first_name, r.last_name
                from
                    movie_%s as l
                inner join
                    %s as r
                    on l.%cid = r.id
                where
                    l.mid = ?""".formatted(getTableName(c), getTableName(c), getTableName(c).charAt(0));

        Function<Class<?>, String> namedSelectAll = (c) -> """
                select
                    r.id, r.name
                from
                    movie_%s as l
                inner join
                    %s as r
                    on l.%cid = r.id
                where
                    l.mid = ?""".formatted(getTableName(c), getTableName(c), getTableName(c).charAt(0));

        String ratingsSelectAll = """
                select
                    r.id, r.name, l.val
                from
                    movie_ratings_agency as l
                inner join
                    ratings_agency as r
                    on l.rid = r.id
                where
                    l.mid = ?""";

        try (var conn = db.getConn();
             var stat = conn.prepareStatement(selectOne)
        ) {

            stat.setInt(1, mid);

            try (var rs = stat.executeQuery()) {
                rs.next();

                var movie = new Movie();

                movie.setId(rs.getInt(1));
                movie.setTitle(rs.getString(2));
                movie.setYear(rs.getInt(3));
                movie.setRuntime(rs.getInt(4));
                movie.setPlot(rs.getString(5));
                movie.setAwards(rs.getString(6));
                movie.setType(rs.getString(7));
                movie.setBoxOffice(rs.getDouble(8));

                try (var a = conn.prepareStatement(personSelectAll.apply(Actor.class));
                     var d = conn.prepareStatement(personSelectAll.apply(Director.class));
                     var g = conn.prepareStatement(namedSelectAll.apply(Genre.class));
                     var l = conn.prepareStatement(namedSelectAll.apply(Language.class));
                     var r = conn.prepareStatement(ratingsSelectAll)) {

                    a.setInt(1, mid);
                    d.setInt(1, mid);
                    g.setInt(1, mid);
                    l.setInt(1, mid);
                    r.setInt(1, mid);

                    try (var ars = a.executeQuery();
                         var drs = d.executeQuery();
                         var grs = g.executeQuery();
                         var lrs = l.executeQuery();
                         var rrs = r.executeQuery()
                    ) {

                        while (ars.next())
                            movie.getActors().add(new Person(ars.getInt(1), ars.getString(2),
                                    ars.getString(3)));


                        while (drs.next())
                            movie.getDirectors().add(new Person(drs.getInt(1), drs.getString(2), drs.getString(3)));

                        while (grs.next())
                            movie.getGenres().add(new Named(grs.getInt(1), grs.getString(2)));

                        while (lrs.next())
                            movie.getLanguages().add(new Named(lrs.getInt(1), lrs.getString(2)));

                        while (rrs.next())
                            movie.getRatings().put(new Named(rrs.getInt(1), rrs.getString(2)), rrs.getFloat(3));


                        return movie;
                    }

                }
            }
        }

    }

    public static boolean deleteOne(DB db, int mid) throws SQLException {
        String deleteQuery = """
                delete from
                    movie
                where
                    id = ?;""";

        try (var conn = db.getConn();
             var stat = conn.prepareStatement(deleteQuery)) {

            stat.setInt(1, mid);

            stat.execute();

            return true;
        }
    }

    public static Movie updateOne(DB db, Movie movie) throws SQLException {
        String updateMovie = """
                update
                    movie
                    (title, year, runtime, plot, awards, tid, box_office) = (?, ?, ?, ?, ?, ?, ?)
                where
                    id = ?;""";

        String insertMovieActor = """
                insert into
                    movie_actor (mid, aid)
                values
                    (?, ?);""";
        String deleteMovieActor = """
                delete from
                    movie_actor
                where
                    mid = ?;""";
        String insertMovieDirector = """
                insert into
                    movie_director (mid, did)
                values
                    (?, ?);""";
        String deleteMovieDirector = """
                delete from
                    movie_director
                where
                    mid = ?;""";
        String insertMovieGenre = """
                insert into
                    movie_genre (mid, gid)
                values
                    (?, ?);""";
        String deleteMovieGenre = """
                delete from
                    movie_genre
                where
                    mid = ?;""";
        String insertMovieLanguage = """
                insert into
                    movie_lang (mid, lid)
                values
                    (?, ?);""";
        String deleteMovieLanguage = """
                delete from
                    movie_lang
                where
                    mid = ?;""";
        String insertMovieRatings = """
                insert into
                    movie_ratings_agency (mid, rid, val)
                values
                    (?, ?, ?)""";
        String deleteMovieRatings = """
                delete from
                    movie_ratings_agency
                where
                    mid = ?;""";

        // insert movie
        try (var conn = db.getConn();
             var stat = conn.prepareStatement(updateMovie);
             var ma = conn.prepareStatement(insertMovieActor);
             var da = conn.prepareStatement(deleteMovieActor);
             var md = conn.prepareStatement(insertMovieDirector);
             var dd = conn.prepareStatement(deleteMovieDirector);
             var mg = conn.prepareStatement(insertMovieGenre);
             var dg = conn.prepareStatement(deleteMovieGenre);
             var ml = conn.prepareStatement(insertMovieLanguage);
             var dl = conn.prepareStatement(deleteMovieLanguage);
             var mr = conn.prepareStatement(insertMovieRatings);
             var dr = conn.prepareStatement(deleteMovieGenre)) {

            stat.setString(1, movie.getTitle());
            stat.setInt(2, movie.getYear());
            stat.setTime(3, new Time(movie.getRuntime()));
            stat.setString(4, movie.getPlot());
            stat.setString(5, movie.getAwards());
            stat.setInt(6, 3);
            stat.setDouble(7, movie.getBoxOffice());
            stat.setInt(8, movie.getId());

            // insert movie...
            try (var rs = stat.executeQuery()) {
                rs.next();
            }

            da.setInt(1, movie.getId());
            da.execute();


            for (var actor : movie.getActors()) {
                ma.setInt(1, movie.getId());
                ma.setInt(2, actor.getId());

                ma.execute();
            }

            dd.setInt(1, movie.getId());
            dd.execute();

            for (var director : movie.getDirectors()) {
                md.setInt(1, movie.getId());
                md.setInt(2, director.getId());

                md.execute();
            }

            dg.setInt(1, movie.getId());
            dg.execute();

            for (var genre : movie.getGenres()) {
                mg.setInt(1, movie.getId());
                mg.setInt(2, genre.getId());

                mg.execute();
            }

            dl.setInt(1, movie.getId());
            dl.execute();

            for (var language : movie.getLanguages()) {
                ml.setInt(1, movie.getId());
                ml.setInt(2, language.getId());

                ml.execute();
            }

            dr.setInt(1, movie.getId());
            dr.execute();

            for (var entry : movie.getRatings().entrySet()) {
                var agency = entry.getKey().getId();
                var val = entry.getValue();

                mr.setInt(1, movie.getId());
                mr.setInt(2, agency);
                mr.setFloat(3, val);

                mr.execute();
            }

            return movie;
        }
    }

    public static Movie insertOne(DB db, Movie movie) throws SQLException {
        String insertMovie = """
                insert into
                    movie (title, year, runtime, plot, awards, tid, box_office)
                values
                    (?, ?, ?, ?, ?, ?, ?)
                returning id;""";

        String insertMovieActor = """
                insert into
                    movie_actor (mid, aid)
                values
                    (?, ?);""";
        String insertMovieDirector = """
                insert into
                    movie_director (mid, did)
                values
                    (?, ?);""";
        String insertMovieGenre = """
                insert into
                    movie_genre (mid, gid)
                values
                    (?, ?);""";
        String insertMovieLanguage = """
                insert into
                    movie_lang (mid, lid)
                values
                    (?, ?);""";
        String insertMovieRatings = """
                insert into
                    movie_ratings_agency (mid, rid, val)
                values
                    (?, ?, ?)""";

        // insert movie
        try (var conn = db.getConn();
             var stat = conn.prepareStatement(insertMovie);
             var ma = conn.prepareStatement(insertMovieActor);
             var md = conn.prepareStatement(insertMovieDirector);
             var mg = conn.prepareStatement(insertMovieGenre);
             var ml = conn.prepareStatement(insertMovieLanguage);
             var mr = conn.prepareStatement(insertMovieRatings)) {

            stat.setString(1, movie.getTitle());
            stat.setInt(2, movie.getYear());
            stat.setInt(3, movie.getRuntime());
            stat.setString(4, movie.getPlot());
            stat.setString(5, movie.getAwards());
            stat.setInt(6, 3);
            stat.setDouble(7, movie.getBoxOffice());

            // insert movie...
            try (var rs = stat.executeQuery()) {
                rs.next();

                movie.setId(rs.getInt(1));
            }

            for (var actor : movie.getActors()) {
                ma.setInt(1, movie.getId());
                ma.setInt(2, actor.getId());

                ma.execute();
            }

            for (var director : movie.getDirectors()) {
                md.setInt(1, movie.getId());
                md.setInt(2, director.getId());

                md.execute();
            }

            for (var genre : movie.getGenres()) {
                mg.setInt(1, movie.getId());
                mg.setInt(2, genre.getId());

                mg.execute();
            }

            for (var language : movie.getLanguages()) {
                ml.setInt(1, movie.getId());
                ml.setInt(2, language.getId());

                ml.execute();
            }

            for (var entry : movie.getRatings().entrySet()) {
                var agency = entry.getKey().getId();
                var val = entry.getValue();

                mr.setInt(1, movie.getId());
                mr.setInt(2, agency);
                mr.setFloat(3, val);

                mr.execute();
            }

            return movie;
        }
    }

    private static String getTableName(Class<?> clazz) {
        if (clazz.isAnnotationPresent(Table.class)) {
            return clazz.getAnnotation(Table.class).name();
        }

        return clazz.getSimpleName().toLowerCase();
    }

    public static ArrayList<Movie> getAll(DB db) throws SQLException {
        var movies = new ArrayList<Movie>();


        String selectAll = """
                select
                    m.id, m.title, m.year, m.runtime, m.plot, m.awards, t.val, m.box_office
                from
                    movie as m
                inner join
                    type as t
                    on m.tid = t.id""";


        Function<Class<?>, String> personSelectAll = (c) -> """
                select
                    r.id, r.first_name, r.last_name
                from
                    movie_%s as l
                inner join
                    %s as r
                    on l.%cid = r.id
                where
                    l.mid = ?""".formatted(getTableName(c), getTableName(c), getTableName(c).charAt(0));

        Function<Class<?>, String> namedSelectAll = (c) -> """
                select
                    r.id, r.name
                from
                    movie_%s as l
                inner join
                    %s as r
                    on l.%cid = r.id
                where
                    l.mid = ?""".formatted(getTableName(c), getTableName(c), getTableName(c).charAt(0));

        String ratingsSelectAll = """
                select
                    r.id, r.name, l.val
                from
                    movie_ratings_agency as l
                inner join
                    ratings_agency as r
                    on l.rid = r.id
                where
                    l.mid = ?""";

        try (var conn = db.getConn();
             var stat = conn.prepareStatement(selectAll);
             var rs = stat.executeQuery()) {


            while (rs.next()) {
                var movie = new Movie();

                movie.setId(rs.getInt(1));
                movie.setTitle(rs.getString(2));
                movie.setYear(rs.getInt(3));
                movie.setRuntime(rs.getInt(4));
                movie.setPlot(rs.getString(5));
                movie.setAwards(rs.getString(6));
                movie.setType(rs.getString(7));
                movie.setBoxOffice(rs.getDouble(8));

                var mid = movie.getId();

                try (var a = conn.prepareStatement(personSelectAll.apply(Actor.class));
                     var d = conn.prepareStatement(personSelectAll.apply(Director.class));
                     var g = conn.prepareStatement(namedSelectAll.apply(Genre.class));
                     var l = conn.prepareStatement(namedSelectAll.apply(Language.class));
                     var r = conn.prepareStatement(ratingsSelectAll)) {

                    a.setInt(1, mid);
                    d.setInt(1, mid);
                    g.setInt(1, mid);
                    l.setInt(1, mid);
                    r.setInt(1, mid);

                    try (var ars = a.executeQuery();
                         var drs = d.executeQuery();
                         var grs = g.executeQuery();
                         var lrs = l.executeQuery();
                         var rrs = r.executeQuery()
                    ) {

                        while (ars.next())
                            movie.getActors().add(new Person(ars.getInt(1), ars.getString(2),
                                    ars.getString(3)));


                        while (drs.next())
                            movie.getDirectors().add(new Person(drs.getInt(1), drs.getString(2), drs.getString(3)));

                        while (grs.next())
                            movie.getGenres().add(new Named(grs.getInt(1), grs.getString(2)));

                        while (lrs.next())
                            movie.getLanguages().add(new Named(lrs.getInt(1), lrs.getString(2)));

                        while (rrs.next())
                            movie.getRatings().put(new Named(rrs.getInt(1), rrs.getString(2)), rrs.getFloat(3));


                        movies.add(movie);
                    }


                }

            }

            return movies;
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getRuntime() {
        return runtime;
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }

    public String getPlot() {
        return plot;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    public String getAwards() {
        return awards;
    }

    public void setAwards(String awards) {
        this.awards = awards;
    }

    public double getBoxOffice() {
        return boxOffice;
    }

    public void setBoxOffice(double boxOffice) {
        this.boxOffice = boxOffice;
    }

    public List<Person> getActors() {
        return actors;
    }

    public void setActors(List<Person> actors) {
        this.actors = actors;
    }

    public List<Person> getDirectors() {
        return directors;
    }

    public void setDirectors(List<Person> directors) {
        this.directors = directors;
    }

    public List<Named> getGenres() {
        return genres;
    }

    public void setGenres(List<Named> genres) {
        this.genres = genres;
    }

    public List<Named> getLanguages() {
        return languages;
    }

    public void setLanguages(List<Named> languages) {
        this.languages = languages;
    }

    public Map<Named, Float> getRatings() {
        return ratings;
    }

    public void setRatings(Map<Named, Float> ratings) {
        this.ratings = ratings;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "title='" + title + '\'' +
                ", year=" + year +
                ", runtime=" + runtime +
                ", plot='" + plot + '\'' +
                ", awards='" + awards + '\'' +
                ", boxOffice=" + boxOffice +
                ", actors=" + actors +
                ", directors=" + directors +
                ", genres=" + genres +
                ", languages=" + languages +
                ", ratings=" + ratings +
                ", id=" + id +
                '}';
    }
}
