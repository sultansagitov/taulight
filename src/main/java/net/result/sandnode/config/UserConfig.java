package net.result.sandnode.config;

import net.result.sandnode.exceptions.ConfigurationException;
import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.asymmetric.Asymmetric;
import net.result.sandnode.util.encryption.asymmetric.AsymmetricKeyStorage;
import net.result.sandnode.util.encryption.asymmetric.interfaces.IAsymmetricConvertor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class UserConfig {
    private static final Logger LOGGER = LogManager.getLogger(UserConfig.class);

    private final JSONObject KEYS_JSON;
    private final String PROPERTIES_FILE;
    private final Path FOLDER_PATH;
    private final Path KEYS_PATH;
    private final Path KEYS_JSON_PATH;

    public UserConfig() throws IOException {
        this("user.properties");
    }

    public UserConfig(@NotNull String fileName) throws IOException {
        PROPERTIES_FILE = fileName;

        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            properties.load(input);
        } catch (Exception e) {
            throw new ConfigurationException("Error when reading \"%s\"".formatted(fileName), e);
        }

        FOLDER_PATH = PathUtil.resolveHomeInPath(properties.getProperty("client.dir_path"));
        KEYS_PATH = Paths.get(FOLDER_PATH.toString(), properties.getProperty("keys.dir_path"));
        if (KEYS_PATH.toFile().mkdirs()) {
            LOGGER.info("Directory created: {}", KEYS_PATH);
        }
        KEYS_JSON_PATH = Paths.get(KEYS_PATH.toString(), properties.getProperty("keys.json_file_name"));
        if (Files.exists(KEYS_JSON_PATH)) {
            if (!Files.isDirectory(KEYS_JSON_PATH)) {
                KEYS_JSON = new JSONObject(new String(Files.readAllBytes(KEYS_JSON_PATH)));
            } else {
                LOGGER.error("\"{}\" is a directory", KEYS_JSON_PATH);
                throw new FileSystemException("\"%s\" is a directory".formatted(KEYS_JSON_PATH));
            }
        } else {
            KEYS_JSON = new JSONObject().put("keys", new JSONArray());
            if (KEYS_JSON_PATH.toFile().createNewFile()) {
                LOGGER.info("File created {}", KEYS_JSON_PATH);
            }
            try (FileWriter fileWriter = new FileWriter(KEYS_JSON_PATH.toFile())) {
                fileWriter.write(KEYS_JSON.toString());
            }
        }
    }

    public void saveKeysJSON() throws IOException {
        try (FileWriter fileWriter = new FileWriter(KEYS_JSON_PATH.toFile())) {
            fileWriter.write(KEYS_JSON.toString());
        }
    }

    public String getPropertiesFile() {
        return PROPERTIES_FILE;
    }

    public Path getFolderPath() {
        return FOLDER_PATH;
    }

    public Path getKeysPath() {
        return KEYS_PATH;
    }

    public JSONObject getKeysJSON() {
        return KEYS_JSON;
    }

    public @Nullable AsymmetricKeyStorage getPublicKey(@NotNull String host, int port) throws IOException,
            CannotUseEncryption, NoSuchEncryptionException, CreatingKeyException {
        for (Object o : KEYS_JSON.getJSONArray("keys")) {
            JSONObject keyObject = (JSONObject) o;
            if (keyObject.getString("host").equalsIgnoreCase(host) && (keyObject.getInt("port") == port)) {
                int encryptionByte = keyObject.getInt("encryption");
                Encryption encryption = Encryption.fromByte((byte) encryptionByte);

                String path = keyObject.getString("path");

                String string = Files.readString(Path.of(path));
                IAsymmetricConvertor publicConvertor = Asymmetric.getPublicConvertor(encryption);

                return publicConvertor.toKeyStorage(string);
            }
        }

        return null;
    }

}
