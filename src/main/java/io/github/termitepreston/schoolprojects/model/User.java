package io.github.termitepreston.schoolprojects.model;

import io.github.termitepreston.schoolprojects.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;

public class User {
    private static final String selectUsersQuery = """
            select
                username, password
            from
                users
            where username = ?;
            """;
    private final DB db;
    private String username;

    public User(DB db) {
        this.db = db;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                '}';
    }

    public void login(String username, String password) throws Exception {
        try (Connection conn = db.getConn()) {
            PreparedStatement stat = conn.prepareStatement(selectUsersQuery);

            stat.setString(1, username);

            String dbPass = null;

            try (ResultSet rs = stat.executeQuery()) {
                // if we did not find any user
                while (rs.next()) {
                    dbPass = rs.getString(2);
                }


                System.out.println("dbPass = " + dbPass);

                if (!Objects.equals(dbPass, password))
                    throw new Exception("Password is incorrect!");

                this.username = username;
            }

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}