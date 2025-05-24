package net.result.main.config;

import net.result.main.exception.crypto.KeyHashCheckingException;
import net.result.sandnode.config.ClientConfig;
import net.result.sandnode.config.KeyEntry;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.encryption.interfaces.SymmetricEncryption;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.crypto.*;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
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

public class ClientPropertiesConfig implements ClientConfig {
    private static final Logger LOGGER = LogManager.getLogger(ClientPropertiesConfig.class);
    private final Path KEYS_JSON_PATH;
    private final Path KEYS_PATH;
    private final SymmetricEncryption SYMMETRIC_ENCRYPTION;
    private final Collection<KeyRecord> serverKeys = new ArrayList<>();
    private final Collection<MemberKeyRecord> memberKeys = new ArrayList<>();
    private final Collection<MemberKeyRecord> DEKs = new ArrayList<>();

    public ClientPropertiesConfig()
            throws ConfigurationException, FSException, NoSuchEncryptionException, EncryptionTypeException {
        this("taulight.properties");
    }

    public ClientPropertiesConfig(@NotNull String fileName)
            throws ConfigurationException, FSException, NoSuchEncryptionException, EncryptionTypeException {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            properties.load(input);
        } catch (IOException e) {
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

        for (Object key : new JSONObject(data).getJSONArray("server-keys")) {
            JSONObject jsonObject = (JSONObject) key;
            try {
                KeyRecord keyRecord = KeyRecord.fromJSON(jsonObject);
                serverKeys.add(keyRecord);
            } catch (NoSuchEncryptionException | CreatingKeyException | FSException | NoSuchHasherException |
                     EncryptionTypeException | InvalidEndpointSyntax | KeyHashCheckingException e) {
                LOGGER.error("Error while validating \"{}\"", KEYS_JSON_PATH, e);
            }
        }

        for (Object key : new JSONObject(data).getJSONArray("member-keys")) {
            JSONObject jsonObject = (JSONObject) key;
            try {
                MemberKeyRecord keyRecord = MemberKeyRecord.fromJSON(jsonObject);
                memberKeys.add(keyRecord);
            } catch (NoSuchEncryptionException | CreatingKeyException | EncryptionTypeException e) {
                LOGGER.error("Error while validating \"{}\"", KEYS_JSON_PATH, e);
            }
        }

        for (Object key : new JSONObject(data).getJSONArray("deks")) {
            JSONObject jsonObject = (JSONObject) key;
            try {
                MemberKeyRecord keyRecord = MemberKeyRecord.fromJSON(jsonObject);
                DEKs.add(keyRecord);
            } catch (NoSuchEncryptionException | CreatingKeyException | EncryptionTypeException e) {
                LOGGER.error("Error while validating \"{}\"", KEYS_JSON_PATH, e);
            }
        }
    }

    @Override
    public @NotNull SymmetricEncryption symmetricKeyEncryption() {
        return SYMMETRIC_ENCRYPTION;
    }

    private JSONObject getKeysJson() {
        JSONArray serverKeyArray = new JSONArray();
        serverKeys.stream().map(KeyRecord::toJSON).forEach(serverKeyArray::put);

        JSONArray memberKeyArray = new JSONArray();
        memberKeys.stream().map(MemberKeyRecord::toJSON).forEach(memberKeyArray::put);

        JSONArray DEKArray = new JSONArray();
        DEKs.stream().map(MemberKeyRecord::toJSON).forEach(DEKArray::put);

        return new JSONObject()
                .put("server-keys", serverKeyArray)
                .put("member-keys", memberKeyArray)
                .put("deks", DEKArray);
    }

    private boolean isHaveKey(@NotNull Endpoint endpoint) {
        return serverKeys.stream().anyMatch(record -> endpoint.equals(record.endpoint));
    }

    public synchronized void saveKeysJSON() throws FSException {
        String string = getKeysJson().toString();
        try (FileWriter fileWriter = new FileWriter(KEYS_JSON_PATH.toFile())) {
            fileWriter.write(string);
        } catch (IOException e) {
            throw new FSException(e);
        }
    }

    @Override
    public synchronized void saveKey(@NotNull Endpoint endpoint, @NotNull AsymmetricKeyStorage keyStorage)
            throws FSException, KeyAlreadySaved {
        String sanitizedEndpoint = endpoint.toString().replaceAll("[.:\\\\/*?\"<>|]", "_");
        String filename = "%s_%s_public.key".formatted(sanitizedEndpoint, UUID.randomUUID());

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
        serverKeys.add(keyRecord);
        saveKeysJSON();
    }

    @Override
    public AsymmetricKeyStorage getPublicKey(@NotNull Endpoint endpoint) throws KeyStorageNotFoundException {
        return serverKeys.stream()
                .filter(keyRecord -> keyRecord.endpoint.equals(endpoint))
                .findFirst()
                .map(keyRecord -> keyRecord.keyStorage.asymmetric())
                .orElseThrow(() -> new KeyStorageNotFoundException(endpoint.toString()));
    }

    @Override
    public synchronized void savePersonalKey(UUID keyID, KeyStorage keyStorage) throws FSException {
        memberKeys.add(new MemberKeyRecord(keyID, keyStorage));
        saveKeysJSON();
    }

    @Override
    public void saveEncryptor(String nickname, UUID keyID, KeyStorage keyStorage) throws FSException {
        LOGGER.debug("{}, {}, {}", nickname, keyID, keyStorage);
        memberKeys.add(new MemberKeyRecord(nickname, keyID, keyStorage));
        saveKeysJSON();
    }

    @Override
    public void saveDEK(String nickname, UUID keyID, KeyStorage keyStorage) throws FSException {
        LOGGER.debug("{}, {}, {}", nickname, keyID, keyStorage);
        DEKs.add(new MemberKeyRecord(nickname, keyID, keyStorage));
        saveKeysJSON();
    }

    @Override
    public synchronized KeyStorage loadPersonalKey(UUID keyID) throws KeyStorageNotFoundException {
        return memberKeys.stream()
                .filter(k -> k.keyID.equals(keyID))
                .map(k -> k.keyStorage)
                .findFirst()
                .orElseThrow(() -> new KeyStorageNotFoundException(keyID));
    }

    @Override
    public KeyEntry loadEncryptor(String nickname) throws KeyStorageNotFoundException {
        return memberKeys.stream()
                .filter(k -> Objects.equals(k.nickname, nickname))
                .map(k -> new KeyEntry(k.keyID, k.keyStorage))
                .findFirst()
                .orElseThrow(() -> new KeyStorageNotFoundException(nickname));
    }

    @Override
    public KeyEntry loadDEK(String nickname) throws KeyStorageNotFoundException {
        return DEKs.stream()
                .filter(k -> Objects.equals(k.nickname, nickname))
                .map(k -> new KeyEntry(k.keyID, k.keyStorage))
                .findFirst()
                .orElseThrow(() -> new KeyStorageNotFoundException(nickname));
    }

    @Override
    public KeyStorage loadDEK(UUID keyID) throws KeyStorageNotFoundException {
        return DEKs.stream()
                .filter(k -> Objects.equals(k.keyID, keyID))
                .map(k -> k.keyStorage)
                .findFirst()
                .orElseThrow(() -> new KeyStorageNotFoundException(keyID));
    }
}
