package net.result.main.config;

import net.result.sandnode.config.MariaDBConfig;
import net.result.sandnode.exception.ConfigurationException;
import net.result.sandnode.exception.ImpossibleRuntimeException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MariaDBPropertiesConfig implements MariaDBConfig {
    private final String URL;
    private final String USER;
    private final String PASSWORD;

    public MariaDBPropertiesConfig() throws ConfigurationException {
        this("taulight.properties");
    }

    public MariaDBPropertiesConfig(String fileName) throws ConfigurationException {
        Properties properties = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input == null)
                throw new ImpossibleRuntimeException("Unable to find %s".formatted(fileName));
            properties.load(input);
        } catch (IOException e) {
            throw new ConfigurationException("Failed to load configuration file", fileName);
        }

        URL = properties.getProperty("hub.db.url");
        USER = properties.getProperty("hub.db.user");
        PASSWORD = properties.getProperty("hub.db.password");
    }

    @Override
    public String getURL() {
        return URL;
    }

    @Override
    public String getUser() {
        return USER;
    }

    @Override
    public String getPassword() {
        return PASSWORD;
    }
}
