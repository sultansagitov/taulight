package net.result.sandnode.config;

import net.result.sandnode.exceptions.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ini4j.Ini;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;


public class ServerConfigSingleton {
    private static final Logger LOGGER = LogManager.getLogger(ServerConfigSingleton.class);
    private static final ServerConfigSingleton instance;

    static {
        try {
            instance = new ServerConfigSingleton("server.ini");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final int PORT;
    private final Path CONF_DIR;
    private final String INFO_MIME;
    private final String INFO;

    private final Path KEYS_DIR;
    private final boolean RSA;
    private Path RSA_PUBLIC_KEY_PATH;
    private Path RSA_PRIVATE_KEY_PATH;

    private ServerConfigSingleton(@NotNull String fileName) throws IOException {
        final Ini ini;
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) throw new IOException("Unable to find " + fileName);

            ini = new Ini();
            ini.load(input);
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }

        PORT = Integer.parseInt(ini.get("Server", "port"));
        CONF_DIR = PathUtil.resolveHomeInPath(Paths.get(ini.get("Server", "dir_path")));
        PathUtil.createDir(CONF_DIR);

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) throw new IOException("Unable to find " + fileName);
            Path path = Paths.get(CONF_DIR.toString(), ini.get("Server.Info", "file_path"));
            final String content;
            try {
                content = new String(Files.readAllBytes(path));
            } catch (NoSuchFileException e) {
                LOGGER.error("Cannot find file {}", path);
                throw e;
            }
            INFO_MIME = ini.get("Server.Info", "mime_type");
            INFO = content;
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }

        KEYS_DIR = PathUtil.resolveHomeInPath(Paths.get(CONF_DIR.toString(), ini.get("Keys", "dir_path")));
        PathUtil.createDir(KEYS_DIR);

        RSA = Objects.equals(ini.get("Keys.RSA", "use"), "true");

        if (RSA) {
            RSA_PUBLIC_KEY_PATH = PathUtil.resolveHomeInPath(Paths.get(KEYS_DIR.toString(), ini.get("Keys.RSA", "public_key_path")));
            RSA_PRIVATE_KEY_PATH = PathUtil.resolveHomeInPath(Paths.get(KEYS_DIR.toString(), ini.get("Keys.RSA", "private_key_path")));
        }
    }

    public static ServerConfigSingleton getInstance() throws IOException {
        return instance;
    }


    public static int getPort() {
        return instance.PORT;
    }

    public static Path getConfDir() {
        return instance.CONF_DIR;
    }

    public static Path getKeysDir() {
        return instance.KEYS_DIR;
    }

    public static Path getRSAPublicKeyPath() {
        return instance.RSA_PUBLIC_KEY_PATH;
    }

    public static Path getRSAPrivateKeyPath() {
        return instance.RSA_PRIVATE_KEY_PATH;
    }

    public static boolean useRSA() {
        return instance.RSA;
    }

    public static String getInfoMIME() {
        return instance.INFO_MIME;
    }

    public static String getInfo() {
        return instance.INFO;
    }
}
