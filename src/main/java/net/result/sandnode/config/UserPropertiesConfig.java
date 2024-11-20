package net.result.sandnode.config;

import net.result.sandnode.exceptions.ConfigurationException;
import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.util.FileUtil;
import net.result.sandnode.util.encryption.Encryptions;
import net.result.sandnode.util.encryption.interfaces.*;
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

public class UserPropertiesConfig implements IUserConfig {
    private static final Logger LOGGER = LogManager.getLogger(UserPropertiesConfig.class);

    private final JSONObject KEYS_JSON;
    private final Path KEYS_JSON_PATH;
    private final IAsymmetricEncryption MAIN_ENCRYPTION;
    private final ISymmetricEncryption SYMMETRIC_ENCRYPTION;

    public UserPropertiesConfig() throws IOException, NoSuchEncryptionException, ConfigurationException, CannotUseEncryption {
        this("user.properties");
    }

    public UserPropertiesConfig(@NotNull String fileName) throws IOException, ConfigurationException, NoSuchEncryptionException, CannotUseEncryption {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            properties.load(input);
        } catch (Exception e) {
            throw new ConfigurationException(String.format("Error when reading \"%s\"", fileName), e);
        }

        MAIN_ENCRYPTION = Encryptions.findAsymmetric(properties.getProperty("keys.main", "RSA").toUpperCase());
        SYMMETRIC_ENCRYPTION = Encryptions.findSymmetric(properties.getProperty("keys.symmetric", "AES").toUpperCase());

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
    public @NotNull IAsymmetricEncryption getMainEncryption() {
        return MAIN_ENCRYPTION;
    }

    @Override
    public @NotNull ISymmetricEncryption getSymmetricKeyEncryption() {
        return SYMMETRIC_ENCRYPTION;
    }

    @Override
    public void addKey(@NotNull Endpoint endpoint, @NotNull IKeyStorage keyStorage) throws ReadingKeyException,
            IOException {
        String filename = String.format(
                "%s_%s",
                endpoint.toString().replaceAll("[.:\\\\/*?\"<>|]", "_"),
                UUID.randomUUID().toString().substring(0, 6)
        );

        IEncryption encryption1 = keyStorage.encryption();
        IAsymmetricEncryption encryption = (IAsymmetricEncryption) encryption1;

        Path publicKeyPath = Paths.get(KEYS_JSON_PATH.getParent().toString(), filename + "_public.key");

        encryption.keySaver().savePublicKey(publicKeyPath, keyStorage);


        JSONObject json = new JSONObject()
                .put("endpoint", endpoint.toString())
                .put("encryption", encryption.asByte())
                .put("path", publicKeyPath.toString());

        try (FileWriter fileWriter = new FileWriter(KEYS_JSON_PATH.toFile(), true)) {
            fileWriter.write(json.toString() + System.lineSeparator());
            LOGGER.info("Key information saved to JSON: {}", json);
        } catch (IOException e) {
            LOGGER.error("Failed to write key information to JSON file", e);
            throw e;
        }


        JSONArray keys = KEYS_JSON.getJSONArray("keys");
        for (Object key : keys) {
            JSONObject object = (JSONObject) key;
            if (object.getString("endpoint").equals(endpoint.toString())) {
                throw new RuntimeException("JSON already have this endpoint");
            }
        }

        keys.put(json);
        saveKeysJSON();
    }

    @Override
    public void saveKeysJSON() throws IOException {
        try (FileWriter fileWriter = new FileWriter(KEYS_JSON_PATH.toFile())) {
            fileWriter.write(KEYS_JSON.toString());
        }
    }

    @Override
    public @Nullable IAsymmetricKeyStorage getPublicKey(@NotNull Endpoint endpoint) throws IOException,
            NoSuchEncryptionException, CreatingKeyException, CannotUseEncryption {
        for (Object o : KEYS_JSON.getJSONArray("keys")) {
            JSONObject keyObject = (JSONObject) o;
            if (keyObject.getString("endpoint").equalsIgnoreCase(endpoint.toString())) {
                int encryptionByte = keyObject.getInt("encryption");
                IAsymmetricEncryption encryption = Encryptions.findAsymmetric((byte) encryptionByte);

                String path = keyObject.getString("path");

                String string = Files.readString(KEYS_JSON_PATH.resolve(path));
                IAsymmetricConvertor publicConvertor = encryption.publicKeyConvertor();

                return publicConvertor.toKeyStorage(string);
            }
        }

        return null;
    }
}
