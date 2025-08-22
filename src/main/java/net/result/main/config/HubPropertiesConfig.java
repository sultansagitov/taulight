package net.result.main.config;

import net.result.sandnode.config.HubConfig;
import net.result.sandnode.exception.ConfigurationException;
import net.result.sandnode.security.PasswordHasher;
import net.result.sandnode.security.PasswordHashers;
import net.result.sandnode.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;

public class HubPropertiesConfig implements HubConfig {
    private final String NAME;
    private final PasswordHasher HASHER;
    private final Path IMAGE_PATH;

    @SuppressWarnings("unused")
    public HubPropertiesConfig() {
        this("taulight.properties");
    }

    public HubPropertiesConfig(@NotNull String fileName) {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            properties.load(input);
        } catch (IOException e) {
            throw new ConfigurationException("Error when reading \"%s\"".formatted(fileName), e);
        }

        NAME = properties.getProperty("hub.name");

        String hasherName = properties.getProperty("hub.hasher");
        HASHER = Arrays.stream(PasswordHashers.values())
                .filter(h -> h.name().equalsIgnoreCase(hasherName))
                .findFirst()
                .orElse(PasswordHashers.values()[0]);

        IMAGE_PATH = FileUtil.resolveHome(Path.of(properties.getProperty("hub.db.dir_path.images")));
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public PasswordHasher hasher() {
        return HASHER;
    }

    @Override
    public Path imagePath() {
        return IMAGE_PATH;
    }
}
