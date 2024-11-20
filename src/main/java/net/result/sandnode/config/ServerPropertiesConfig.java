package net.result.sandnode.config;

import net.result.sandnode.exceptions.ConfigurationException;
import net.result.sandnode.util.Endpoint;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

public class ServerPropertiesConfig implements IServerConfig {
    private final Endpoint endpoint;

    public ServerPropertiesConfig() throws ConfigurationException {
        this("server.properties", null);
    }

    public ServerPropertiesConfig(Endpoint endpoint) throws ConfigurationException {
        this("server.properties", endpoint);
    }

    public ServerPropertiesConfig(@NotNull String fileName, @Nullable Endpoint endpoint) throws ConfigurationException {
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

    @Override
    public Endpoint getEndpoint() {
        return endpoint;
    }

}
