package net.result.sandnode.config;

import net.result.sandnode.exceptions.ConfigurationException;
import net.result.sandnode.util.Endpoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public class ServerConfig {
    private final Endpoint endpoint;

    public ServerConfig() {
        this("server.properties", null);
    }

    public ServerConfig(Endpoint endpoint) {
        this("server.properties", endpoint);
    }

    public ServerConfig(@NotNull String fileName, @Nullable Endpoint endpoint) {
        Properties properties = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input == null)
                throw new RuntimeException(String.format("Unable to find %s", fileName));
            properties.load(input);
        } catch (IOException e) {
            throw new ConfigurationException(String.format("Failed to load configuration file: %s", fileName));
        }

        String defaultHost = "127.0.0.1";
        int defaultPort = 52525;

        this.endpoint = Objects.requireNonNullElseGet(endpoint, () -> new Endpoint(
                properties.getProperty("server.host", defaultHost),
                Integer.parseInt(properties.getProperty("server.port", "" + defaultPort))
        ));
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

}
