package io.github.termitepreston.schoolprojects.model;

import io.github.termitepreston.schoolprojects.DB;

import java.security.Principal;
import java.sql.SQLException;
import java.util.Objects;

public class User implements Principal {
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

    public boolean isAdmin() {
        return getName().equals("admin");
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

    @Override
    public String getName() {
        return username;
    }

    public void login(String username, String password) throws UserNotFoundException, SQLException, WrongPasswordException {
        try (var conn = db.getConn();
             var stat = conn.prepareStatement(selectUsersQuery)) {

            stat.setString(1, username);

            String dbPass = null;

            try (var rs = stat.executeQuery()) {
                // if we did not find any user
                while (rs.next()) {
                    dbPass = rs.getString(2);
                }

                if (dbPass == null)
                    throw new UserNotFoundException("User " + username + " not found!");

                if (!Objects.equals(dbPass, password))
                    throw new WrongPasswordException("Incorrect password entered!");

                this.username = username;
            }

        }
    }
}
