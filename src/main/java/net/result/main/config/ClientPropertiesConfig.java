package net.result.main.config;

import net.result.sandnode.config.IClientConfig;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.ISymmetricEncryption;
import net.result.sandnode.exception.*;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.util.FileUtil;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ClientPropertiesConfig implements IClientConfig {
    private static final Logger LOGGER = LogManager.getLogger(ClientPropertiesConfig.class);
    private final Path KEYS_JSON_PATH;
    private final Path KEYS_PATH;
    private final ISymmetricEncryption SYMMETRIC_ENCRYPTION;
    private final Collection<KeyRecord> records = new ArrayList<>();

    public ClientPropertiesConfig()
            throws ConfigurationException, FSException, NoSuchEncryptionException, EncryptionTypeException {
        this("taulight.properties");
    }

    public ClientPropertiesConfig(@NotNull String fileName)
            throws ConfigurationException, FSException, NoSuchEncryptionException, EncryptionTypeException {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            properties.load(input);
        } catch (Exception e) {
            throw new ConfigurationException("Error when reading \"%s\"".formatted(fileName), e);
        }

        KEYS_PATH = FileUtil.resolveHome(properties.getProperty("client.keys.dir_path"));

        if (KEYS_PATH.toFile().mkdirs())
            LOGGER.info("Directory created: {}", KEYS_PATH);

        KEYS_JSON_PATH = FileUtil.resolveHome(properties.getProperty("client.keys.json_file_name"));

        String symKeyProperty = properties.getProperty("client.keys.symmetric");
        SYMMETRIC_ENCRYPTION = EncryptionManager.find(symKeyProperty).symmetric();

        if (!Files.exists(KEYS_JSON_PATH)) {
            FileUtil.createFile(KEYS_JSON_PATH);
            saveKeysJSON();
            return;
        }

        FileUtil.checkNotDirectory(KEYS_JSON_PATH);
        String data = FileUtil.readString(KEYS_JSON_PATH);
        for (Object key : new JSONObject(data).getJSONArray("keys")) {
            JSONObject jsonObject = (JSONObject) key;
            try {
                KeyRecord keyRecord = KeyRecord.fromJSON(jsonObject);
                records.add(keyRecord);
            } catch (SandnodeSecurityException | NoSuchEncryptionException | CreatingKeyException | FSException |
                     NoSuchHasherException | EncryptionTypeException | InvalidEndpointSyntax e) {
                LOGGER.error("Error while validating \"{}\"", KEYS_JSON_PATH, e);
            }
        }
    }

    private JSONObject getKeysJson() {
        JSONArray array = new JSONArray();
        records.stream().map(KeyRecord::toJSON).forEach(array::put);
        return new JSONObject().put("keys", array);
    }

    @Override
    public @NotNull ISymmetricEncryption symmetricKeyEncryption() {
        return SYMMETRIC_ENCRYPTION;
    }

    @Override
    public synchronized void saveKey(
            @NotNull Endpoint endpoint,
            @NotNull AsymmetricKeyStorage keyStorage
    ) throws FSException, KeyAlreadySaved {
        String filename = "%s_%s_public.key".formatted(
                endpoint.toString().replaceAll("[.:\\\\/*?\"<>|]", "_"),
                UUID.randomUUID().toString().substring(0, 6)
        );

        if (isHaveKey(endpoint))
            throw new KeyAlreadySaved("JSON already have this endpoint");

        Path publicKeyPath = Paths.get(KEYS_PATH.toString(), filename);

        if (Files.exists(publicKeyPath)) {
            FileUtil.deleteFileWithoutChecking(publicKeyPath);
        }
        LOGGER.info("Public key file will be created at path: {}", publicKeyPath);

        String publicKeyString;
        try {
            publicKeyString = keyStorage.encodedPublicKey();
        } catch (CannotUseEncryption e) {
            throw new ImpossibleRuntimeException(e);
        }

        try (FileWriter publicKeyWriter = new FileWriter(publicKeyPath.toString())) {
            LOGGER.info("Writing public key to file.");
            publicKeyWriter.write(publicKeyString);

            LOGGER.info("Setting public key file permissions.");
            FileUtil.makeOwnerOnlyRead(publicKeyPath);
        } catch (IOException e) {
            throw new FSException("Error writing public key to file", e);
        }

        KeyRecord keyRecord = new KeyRecord(publicKeyPath, keyStorage, endpoint, publicKeyString);
        records.add(keyRecord);
        saveKeysJSON();
    }

    private boolean isHaveKey(@NotNull Endpoint endpoint) {
        return records.stream().anyMatch(record -> endpoint.toString().equals(record.endpoint.toString()));
    }

    public synchronized void saveKeysJSON() throws FSException {
        try (FileWriter fileWriter = new FileWriter(KEYS_JSON_PATH.toFile())) {
            fileWriter.write(getKeysJson().toString());
        } catch (IOException e) {
            throw new FSException(e);
        }
    }

    @Override
    public synchronized Optional<AsymmetricKeyStorage> getPublicKey(@NotNull Endpoint endpoint) {
        for (KeyRecord keyRecord : records) {
            if (keyRecord.endpoint.toString().equals(endpoint.toString())) {
                return Optional.of(keyRecord.keyStorage.asymmetric());
            }
        }
        return Optional.empty();
    }
}
