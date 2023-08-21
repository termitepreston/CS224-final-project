package io.github.termitepreston.schoolprojects;

import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.command.CommandScope;
import liquibase.command.core.UpdateCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionCommandStep;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DB {
    private final Config config;

    public DB(Config config) throws Exception {
        this.config = config;

        migrate();
    }

    public Config getConfig() {
        return config;
    }

    private void migrate() throws Exception {
        Map<String, Object> config = new HashMap<>();

        Scope.child(config, () -> {
            try (Connection conn = connect()) {
                Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn));

                Liquibase liquibase = new liquibase.Liquibase("schema/changelog-master.xml", new ClassLoaderResourceAccessor(), database);

                CommandScope updateCommand = new CommandScope(UpdateCommandStep.COMMAND_NAME);

                updateCommand.addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, liquibase.getDatabase());
                updateCommand.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, liquibase.getChangeLogFile());
                updateCommand.execute();
            }
        });
    }

    private Connection connect() throws SQLException {
        Properties dbProps = new Properties();

        dbProps.setProperty("user", config.get("db-user"));
        dbProps.setProperty("password", config.get("db-password"));

        return DriverManager.getConnection(config.get("db-url"), dbProps);
    }
}
