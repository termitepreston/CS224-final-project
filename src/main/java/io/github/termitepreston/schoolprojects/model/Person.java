package io.github.termitepreston.schoolprojects.model;

import io.github.termitepreston.schoolprojects.DB;

import java.sql.SQLException;
import java.util.ArrayList;

public class Person {
    private int id;
    private String firstName;
    private String lastName;

    public Person(int id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public static ArrayList<Person> getAll(DB db, Class<? extends Person> clazz) throws SQLException {
        String tableName = clazz.getSimpleName().toLowerCase();


        final String selectAll = """
                select * from %s;
                """.formatted(tableName);

        try (var conn = db.getConn();
             var stat = conn.prepareStatement(selectAll);
             var rs = stat.executeQuery()) {

            ArrayList<Person> people = new ArrayList<>();

            while (rs.next()) {
                people.add(new Person(rs.getInt(1), rs.getString(2), rs.getString(3)));
            }

            return people;

        }
    }

    private static String getTableName(Class<? extends Person> clazz) {
        if (clazz.isAnnotationPresent(Table.class)) {
            return clazz.getAnnotation(Table.class).name();
        }

        return clazz.getSimpleName().toLowerCase();
    }

    public static Person insertOne(DB db, Class<? extends Person> clazz, String firstName, String lastName) throws SQLException {
        String tableName = getTableName(clazz);

        String insertStat = """
                insert into
                    %s (first_name, last_name)
                values
                    (?, ?)
                returning id;""".formatted(tableName);


        try (var conn = db.getConn();
             var stat = conn.prepareStatement(insertStat)
        ) {

            stat.setString(1, firstName);
            stat.setString(2, lastName);

            try (var rs = stat.executeQuery()) {
                rs.next();

                return new Person(rs.getInt(1), firstName, lastName);
            }
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
}
