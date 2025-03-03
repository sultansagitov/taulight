package net.result.main.config;

import com.auth0.jwt.algorithms.Algorithm;
import net.result.sandnode.config.JWTConfig;
import net.result.sandnode.exception.ConfigurationException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class JWTPropertiesConfig implements JWTConfig {
    private final Algorithm ALGORITHM;

    public JWTPropertiesConfig() throws ConfigurationException {
        this("taulight.properties");
    }

    public JWTPropertiesConfig(@NotNull String fileName) throws ConfigurationException {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            properties.load(input);
        } catch (IOException e) {
            throw new ConfigurationException("Error when reading \"%s\"".formatted(fileName), e);
        }

        ALGORITHM = Algorithm.HMAC256(properties.getProperty("hub.jwt_key"));
    }

    @Override
    public Algorithm getAlgorithm() {
        return ALGORITHM;
    }
}
