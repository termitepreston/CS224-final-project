package io.github.termitepreston.schoolprojects;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    private final Properties appConfig;

    public Config(String path) throws IOException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        // open config file with try-with-resource patter.

        try (InputStream stream = loader.getResourceAsStream(path)) {
            appConfig = new Properties();

            appConfig.loadFromXML(stream);
        }
    }

    public Properties getAppConfig() {
        return appConfig;
    }

}
