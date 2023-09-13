package io.github.termitepreston.schoolprojects.model;

import io.github.termitepreston.schoolprojects.DB;

import java.sql.SQLException;
import java.util.ArrayList;

public class Named {
    private int id;
    private String name;

    public Named(int id, String name) {
        this.id = id;
        this.name = name;
    }

    private static String getTableName(Class<? extends Named> clazz) {
        if (clazz.isAnnotationPresent(Table.class)) {
            return clazz.getAnnotation(Table.class).name();
        }

        return clazz.getSimpleName().toLowerCase();
    }

    public static ArrayList<Named> getAll(DB db, Class<? extends Named> clazz) throws SQLException {

        String tableName = getTableName(clazz);


        final String selectAll = """
                select * from %s;
                """.formatted(tableName);

        try (var conn = db.getConn();
             var stat = conn.prepareStatement(selectAll);
             var rs = stat.executeQuery()) {

            ArrayList<Named> namedOnes = new ArrayList<>();

            while (rs.next()) {
                namedOnes.add(new Named(rs.getInt(1), rs.getString(2)));
            }

            return namedOnes;

        }
    }

    public static Named insertOne(DB db, Class<? extends Named> clazz, String name) throws SQLException {
        String tableName = getTableName(clazz);

        String insertStat = """
                insert into
                    %s (name)
                values
                    (?, ?)
                returning id;""".formatted(tableName);

        try (var conn = db.getConn();
             var stat = conn.prepareStatement(insertStat);
             var rs = stat.executeQuery()) {

            rs.next();

            return new Named(rs.getInt(1), name);
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
