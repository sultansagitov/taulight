package net.result.main.config;

import net.result.sandnode.config.HubConfig;
import net.result.sandnode.exception.ConfigurationException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class HubPropertiesConfig implements HubConfig {
    private final String NAME;

    public HubPropertiesConfig() throws ConfigurationException {
        this("taulight.properties");
    }

    public HubPropertiesConfig(@NotNull String fileName) throws ConfigurationException {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            properties.load(input);
        } catch (IOException e) {
            throw new ConfigurationException("Error when reading \"%s\"".formatted(fileName), e);
        }

        NAME = properties.getProperty("hub.name");
    }

    @Override
    public String name() {
        return NAME;
    }
}
