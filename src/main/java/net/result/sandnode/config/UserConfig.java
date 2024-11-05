package net.result.sandnode.config;

import net.result.sandnode.exceptions.ConfigurationException;
import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.util.FileUtil;
import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.asymmetric.Asymmetric;
import net.result.sandnode.util.encryption.asymmetric.AsymmetricKeyStorage;
import net.result.sandnode.util.encryption.asymmetric.interfaces.IAsymmetricConvertor;
import net.result.sandnode.util.encryption.core.interfaces.IKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.UUID;

public class UserConfig implements NodeConfig {
    private static final Logger LOGGER = LogManager.getLogger(UserConfig.class);

    private final JSONObject KEYS_JSON;
    private final Path KEYS_JSON_PATH;
    private final Encryption MAIN_ENCRYPTION;
    private final Encryption SYMMETRIC_ENCRYPTION;

    public UserConfig() throws IOException {
        this("user.properties");
    }

    public UserConfig(@NotNull String fileName) throws IOException {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            properties.load(input);
        } catch (Exception e) {
            throw new ConfigurationException(String.format("Error when reading \"%s\"", fileName), e);
        }

        MAIN_ENCRYPTION = Encryption.valueOf(properties.getProperty("keys.main", "RSA").toUpperCase());
        SYMMETRIC_ENCRYPTION = Encryption.valueOf(properties.getProperty("keys.symmetric", "AES").toUpperCase());

        Path FOLDER_PATH = FileUtil.resolveHomeInPath(properties.getProperty("user.dir_path"));
        Path KEYS_PATH = Paths.get(FOLDER_PATH.toString(), properties.getProperty("keys.dir_path"));

        if (KEYS_PATH.toFile().mkdirs()) LOGGER.info("Directory created: {}", KEYS_PATH);

        KEYS_JSON_PATH = Paths.get(KEYS_PATH.toString(), properties.getProperty("keys.json_file_name"));

        if (Files.exists(KEYS_JSON_PATH)) {
            FileUtil.checkNotDirectory(KEYS_JSON_PATH);
            String data = Files.readString(KEYS_JSON_PATH);
            KEYS_JSON = new JSONObject(data);
        } else {
            KEYS_JSON = new JSONObject().put("keys", new JSONArray());

            FileUtil.createFile(KEYS_JSON_PATH);
            saveKeysJSON();
        }
    }

    @Override
    public @NotNull Encryption getMainEncryption() {
        return MAIN_ENCRYPTION;
    }

    @Override
    public @NotNull Encryption getSymmetricKeyEncryption() {
        return SYMMETRIC_ENCRYPTION;
    }

    public void addKey(@NotNull Endpoint endpoint, @NotNull IKeyStorage keyStorage) throws CannotUseEncryption,
            ReadingKeyException, IOException {
        String filename = endpoint
                .toString()
                .replaceAll("[.:\\\\/*?\"<>|]", "_") +
                "_" +
                UUID.randomUUID().toString().substring(0, 6);

        Encryption encryption = keyStorage.encryption();

        Path publicKeyPath = Paths.get(KEYS_JSON_PATH.getParent().toString(), filename + "_public.key");

        Asymmetric.getKeySaver(encryption).savePublicKey(publicKeyPath, keyStorage);


        JSONObject json = new JSONObject()
                .put("endpoint", endpoint.toString())
                .put("encryption", encryption.asByte())
                .put("path", publicKeyPath.toString());

        try (FileWriter fileWriter = new FileWriter(KEYS_JSON_PATH.toFile(), true)) {
            fileWriter.write(json.toString() + System.lineSeparator());
            LOGGER.info("Key information saved to JSON: {}", json);
        } catch (IOException e) {
            LOGGER.error("Failed to write key information to JSON file", e);
            throw new CannotUseEncryption("Failed to write key information to JSON file", e);
        }


        JSONArray keys = KEYS_JSON.getJSONArray("keys");
        for (Object key : keys) {
            JSONObject object = (JSONObject) key;
            if (object.getString("endpoint").equals(endpoint.toString())) {
                throw new RuntimeException("JSON already have this key");
            }
        }

        keys.put(json);
        saveKeysJSON();
    }


    public void saveKeysJSON() throws IOException {
        try (FileWriter fileWriter = new FileWriter(KEYS_JSON_PATH.toFile())) {
            fileWriter.write(KEYS_JSON.toString());
        }
    }

    public @Nullable AsymmetricKeyStorage getPublicKey(@NotNull Endpoint endpoint) throws IOException,
            CannotUseEncryption, NoSuchEncryptionException, CreatingKeyException {
        for (Object o : KEYS_JSON.getJSONArray("keys")) {
            JSONObject keyObject = (JSONObject) o;
            if (keyObject.getString("endpoint").equalsIgnoreCase(endpoint.toString())) {
                int encryptionByte = keyObject.getInt("encryption");
                Encryption encryption = Encryption.fromByte((byte) encryptionByte);

                String path = keyObject.getString("path");

                String string = Files.readString(KEYS_JSON_PATH.resolve(path));
                IAsymmetricConvertor publicConvertor = Asymmetric.getPublicConvertor(encryption);

                return publicConvertor.toKeyStorage(string);
            }
        }

        return null;
    }
}
