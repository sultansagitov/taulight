package net.result.sandnode.config;

import net.result.sandnode.exceptions.ConfigurationException;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.util.encryption.asymmetric.rsa.RSAKeySaver;
import net.result.sandnode.util.encryption.asymmetric.rsa.RSAKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ini4j.Ini;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Scanner;

import static net.result.sandnode.util.encryption.Encryption.RSA;


public class ServerConfigSingleton {
    private static final Logger LOGGER = LogManager.getLogger(ServerConfigSingleton.class);
    private static final ServerConfigSingleton instance = new ServerConfigSingleton("server.ini");

    private final int PORT;
    private final Path CONF_DIR;
    private final String INFO_MIME;
    private final String INFO;

    private final Path KEYS_DIR;
    private final boolean USES_RSA;
    private Path RSA_PUBLIC_KEY_PATH;
    private Path RSA_PRIVATE_KEY_PATH;

    private ServerConfigSingleton(@NotNull String fileName) {
        Ini ini;
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) throw new RuntimeException("Unable to find " + fileName);

            ini = new Ini();
            ini.load(input);
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }

        PORT = Integer.parseInt(ini.get("Server", "port"));
        CONF_DIR = PathUtil.resolveHomeInPath(Paths.get(ini.get("Server", "dir_path")));
        try {
            PathUtil.createDir(CONF_DIR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) throw new IOException("Unable to find \"%s\"".formatted(fileName));
            Path path = Paths.get(CONF_DIR.toString(), ini.get("Server.Info", "file_path"));
            String content;
            try {
                content = new String(Files.readAllBytes(path));
            } catch (NoSuchFileException e) {
                LOGGER.error("Cannot find file \"{}\", it will be created now", path);
                Scanner scanner = new Scanner(System.in);

                System.out.print("Enter the name: ");
                String name = scanner.nextLine();

                System.out.print("Enter the owner: ");
                String owner = scanner.nextLine();

                System.out.print("Enter the display name: ");
                String displayName = scanner.nextLine();

                JSONObject jsonObject = new JSONObject();

                jsonObject.put("name", name);
                jsonObject.put("data-encryption", "RSA");
                jsonObject.put("sign-encryption", "ECC");
                jsonObject.put("owner", owner);
                jsonObject.put("display-name", displayName);

                content = jsonObject.toString(4);
                System.out.println(content);

                scanner.close();
                PathUtil.createFile(path, content);
            }
            INFO_MIME = ini.get("Server.Info", "mime_type");
            INFO = content;
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }

        USES_RSA = Objects.equals(ini.get("Keys.RSA", "use"), "true");
        KEYS_DIR = PathUtil.resolveHomeInPath(Paths.get(CONF_DIR.toString(), ini.get("Keys", "dir_path")));

        if (USES_RSA) {
            RSA_PUBLIC_KEY_PATH = PathUtil.resolveHomeInPath(Paths.get(KEYS_DIR.toString(), ini.get("Keys.RSA", "public_key_path")));
            RSA_PRIVATE_KEY_PATH = PathUtil.resolveHomeInPath(Paths.get(KEYS_DIR.toString(), ini.get("Keys.RSA", "private_key_path")));
        }

        if (!Files.exists(KEYS_DIR)) {
            System.out.println("KEYS_DIR does not exist, creating it: " + KEYS_DIR);
            try {
                PathUtil.createDir(KEYS_DIR);

            } catch (IOException e) {
                throw new RuntimeException("Error creating KEYS_DIR", e);
            }
        }



        try {
            if (USES_RSA && !RSA_PUBLIC_KEY_PATH.toFile().exists() && !RSA_PRIVATE_KEY_PATH.toFile().exists()) {
                RSAKeySaver.getInstance().saveKeys(
                        (RSAKeyStorage) RSA.generator().generateKeyStorage(),
                        RSA_PUBLIC_KEY_PATH,
                        RSA_PRIVATE_KEY_PATH
                );
            }
        } catch (IOException | ReadingKeyException e) {
            throw new RuntimeException(e);
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
        return instance.USES_RSA;
    }

    public static String getInfoMIME() {
        return instance.INFO_MIME;
    }

    public static String getInfo() {
        return instance.INFO;
    }
}
