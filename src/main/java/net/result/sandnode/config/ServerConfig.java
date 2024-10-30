package net.result.sandnode.config;

import net.result.sandnode.exceptions.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

public class ServerConfig {
    private static final Logger LOGGER = LogManager.getLogger(ServerConfig.class);
    private final InetAddress HOST;
    private final int PORT;

    public ServerConfig() {
        this("server.properties", null);
    }

    public ServerConfig(InetAddress address) {
        this("server.properties", address);
    }

    public ServerConfig(@NotNull String fileName, @Nullable InetAddress address) {
        Properties properties = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input == null)
                throw new RuntimeException("Unable to find %s".formatted(fileName));
            properties.load(input);
        } catch (IOException e) {
            throw new ConfigurationException("Failed to load configuration file: %s".formatted(fileName));
        }

        String defaultHost = "127.0.0.1";
        int defaultPort = 52525;
        try {
            HOST = (address != null)
                    ? address
                    : InetAddress.getByName(properties.getProperty("server.host", defaultHost));
        } catch (UnknownHostException e) {
            LOGGER.error(e);
            throw new RuntimeException(e);
        }
        PORT = Integer.parseInt(properties.getProperty("server.port", "" + defaultPort));
    }

    public InetAddress getHost() {
        return HOST;
    }

    public int getPort() {
        return PORT;
    }
}
