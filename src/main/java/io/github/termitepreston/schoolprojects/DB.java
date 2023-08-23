package io.github.termitepreston.schoolprojects;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DB {
    private final Config config;

    public DB(Config config) {
        this.config = config;
    }


    public Connection getConn() throws SQLException {
        Properties dbProps = new Properties();

        dbProps.setProperty("user", config.getAppConfig().getProperty("db-user"));
        dbProps.setProperty("password", config.getAppConfig().getProperty("db-password"));

        return DriverManager.getConnection(config.getAppConfig().getProperty("db-url"), dbProps);
    }
}
