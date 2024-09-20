package net.result.sandnode.config;

import net.result.sandnode.exceptions.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ini4j.Ini;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;


public class ServerConfigSingleton {
    private static final Logger LOGGER = LogManager.getLogger(ServerConfigSingleton.class);
    private static ServerConfigSingleton instance;
    private final int PORT;
    private final String CONF_DIR;
    private final String INFO_MIME;
    private final String INFO;

    private final String KEYS_DIR;
    private final boolean RSA;
    private String RSA_PUBLIC_KEY_PATH;
    private String RSA_PRIVATE_KEY_PATH;

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
        CONF_DIR = resolveHomeInPath(Paths.get(ini.get("Server", "dir_path")));
        createDir(CONF_DIR);

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) throw new IOException("Unable to find " + fileName);
            final String content = new String(Files.readAllBytes(Paths.get(CONF_DIR, ini.get("Server.Info", "file_path"))));
            INFO_MIME = ini.get("Server.Info", "mime_type");
            INFO = content;
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }

        KEYS_DIR = resolveHomeInPath(Paths.get(CONF_DIR, ini.get("Keys", "dir_path")));
        createDir(KEYS_DIR);

        RSA = Objects.equals(ini.get("Keys.RSA", "use"), "true");

        if (RSA) {
            RSA_PUBLIC_KEY_PATH = resolveHomeInPath(Paths.get(KEYS_DIR, ini.get("Keys.RSA", "public_key_path")));
            RSA_PRIVATE_KEY_PATH = resolveHomeInPath(Paths.get(KEYS_DIR, ini.get("Keys.RSA", "private_key_path")));
        }
    }

    private static @NotNull String resolveHomeInPath(@NotNull Path path) {
        final String homeDir = System.getProperty("user.home");
        final String inputPath = path.toString();

        final int tildeIndex = inputPath.indexOf("~");
        final Path resolvedPath;
        if (tildeIndex >= 0) resolvedPath = Paths.get(homeDir, inputPath.substring(tildeIndex + 1));
        else resolvedPath = path.toAbsolutePath();
        return resolvedPath.toString();
    }

    public static ServerConfigSingleton getInstance() throws IOException {
        if (instance == null)
            synchronized (ServerConfigSingleton.class) {
                instance = new ServerConfigSingleton("server.ini");
            }
        return instance;
    }

    public static void createDir(@NotNull String directoryPath) throws IOException {
        final File dir = new File(directoryPath);
        if (!dir.exists()) {
            LOGGER.warn("Directory \"{}\" not found, it will be created now", directoryPath);

            if (dir.mkdirs())
                LOGGER.info("Directory successfully created");
            else {
                final IOException e = new IOException("Failed to create directory");
                LOGGER.error("Failed to create directory \"{}\"", directoryPath, e);
                throw e;
            }
        }
    }


    public static int getPort() {
        return instance.PORT;
    }

    public static String getConfDir() {
        return instance.CONF_DIR;
    }

    public static String getKeysDir() {
        return instance.KEYS_DIR;
    }

    public static String getRSAPublicKeyPath() {
        return instance.RSA_PUBLIC_KEY_PATH;
    }

    public static String getRSAPrivateKeyPath() {
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
