package net.result.main.config;

import net.result.main.exception.crypto.KeyHashCheckingException;
import net.result.sandnode.config.AgentConfig;
import net.result.sandnode.config.KeyEntry;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.crypto.*;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.util.Address;
import net.result.sandnode.util.FileUtil;
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

public class
AgentPropertiesConfig implements AgentConfig {
    private static final Logger LOGGER = LogManager.getLogger(AgentPropertiesConfig.class);
    private final Path KEYS_JSON_PATH;
    private final Path KEYS_PATH;
    private final Collection<KeyRecord> serverKeys = new ArrayList<>();
    private final Collection<MemberKeyRecord> memberKeys = new ArrayList<>();
    private final Collection<MemberKeyRecord> DEKs = new ArrayList<>();

    public AgentPropertiesConfig() throws ConfigurationException, StorageException {
        this("taulight.properties");
    }

    public AgentPropertiesConfig(@NotNull String fileName) throws ConfigurationException, StorageException {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            properties.load(input);
        } catch (IOException e) {
            throw new ConfigurationException("Error when reading \"%s\"".formatted(fileName), e);
        }

        KEYS_PATH = FileUtil.resolveHome(properties.getProperty("agent.keys.dir_path"));

        if (KEYS_PATH.toFile().mkdirs())
            LOGGER.info("Directory created: {}", KEYS_PATH);

        KEYS_JSON_PATH = FileUtil.resolveHome(properties.getProperty("agent.keys.json_file_name"));

        try {
            if (!Files.exists(KEYS_JSON_PATH)) {
                FileUtil.createFile(KEYS_JSON_PATH);
                saveKeysJSON();
                return;
            }

            FileUtil.checkNotDirectory(KEYS_JSON_PATH);
            String data = FileUtil.readString(KEYS_JSON_PATH);
            JSONObject json = new JSONObject(data);

            for (Object key : json.getJSONArray("server-keys")) {
                JSONObject jsonObject = (JSONObject) key;
                try {
                    KeyRecord keyRecord = KeyRecord.fromJSON(jsonObject);
                    serverKeys.add(keyRecord);
                } catch (NoSuchEncryptionException | CreatingKeyException | FSException | NoSuchHasherException |
                         EncryptionTypeException | InvalidAddressSyntax | KeyHashCheckingException e) {
                    LOGGER.error("Error while validating \"{}\"", KEYS_JSON_PATH, e);
                }
            }

            for (Object key : json.getJSONArray("member-keys")) {
                JSONObject jsonObject = (JSONObject) key;
                try {
                    MemberKeyRecord keyRecord = MemberKeyRecord.fromJSON(jsonObject);
                    memberKeys.add(keyRecord);
                } catch (NoSuchEncryptionException | CreatingKeyException | EncryptionTypeException e) {
                    LOGGER.error("Error while validating \"{}\"", KEYS_JSON_PATH, e);
                }
            }

            for (Object key : json.getJSONArray("deks")) {
                JSONObject jsonObject = (JSONObject) key;
                try {
                    MemberKeyRecord keyRecord = MemberKeyRecord.fromJSON(jsonObject);
                    DEKs.add(keyRecord);
                } catch (NoSuchEncryptionException | CreatingKeyException | EncryptionTypeException e) {
                    LOGGER.error("Error while validating \"{}\"", KEYS_JSON_PATH, e);
                }
            }
        } catch (Exception e) {
            throw new StorageException(e);
        }
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

    private boolean isHaveKey(@NotNull Address address) {
        return serverKeys.stream().anyMatch(record -> address.equals(record.address));
    }

    public synchronized void saveKeysJSON() throws StorageException {
        String string = getKeysJson().toString();
        try (FileWriter fileWriter = new FileWriter(KEYS_JSON_PATH.toFile())) {
            fileWriter.write(string);
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    @Override
    public synchronized void saveServerKey(@NotNull Address address, @NotNull AsymmetricKeyStorage keyStorage)
            throws KeyAlreadySaved, StorageException {
        String sanitizedAddress = address.toString().replaceAll("[.:\\\\/*?\"<>|]", "_");
        String filename = "%s_%s_public.key".formatted(sanitizedAddress, UUID.randomUUID());

        if (isHaveKey(address))
            throw new KeyAlreadySaved("JSON already have this address");

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
            if (FileUtil.isPosixSupported()) {
                FileUtil.makeOwnerOnlyRead(publicKeyPath);
            } else {
                LOGGER.warn("POSIX unsupported here");
            }
        } catch (IOException | FSException e) {
            throw new StorageException("Error writing public key to file", e);
        }

        KeyRecord keyRecord = new KeyRecord(publicKeyPath, keyStorage, address, publicKeyString);
        serverKeys.add(keyRecord);
        saveKeysJSON();
    }

    @Override
    public AsymmetricKeyStorage loadServerKey(@NotNull Address address) throws KeyStorageNotFoundException {
        return serverKeys.stream()
                .filter(keyRecord -> keyRecord.address.equals(address))
                .findFirst()
                .map(keyRecord -> keyRecord.keyStorage.asymmetric())
                .orElseThrow(() -> new KeyStorageNotFoundException(address.toString()));
    }

    @Override
    public synchronized void savePersonalKey(Address address, String nickname, KeyStorage keyStorage)
            throws StorageException {
        memberKeys.add(new MemberKeyRecord(address, nickname, keyStorage));
        saveKeysJSON();
    }

    @Override
    public void saveEncryptor(Address address, String nickname, KeyStorage keyStorage)
            throws StorageException {
        LOGGER.debug("Saving encryptor {}@{}", nickname, address);
        memberKeys.add(new MemberKeyRecord(address, nickname, keyStorage));
        saveKeysJSON();
    }

    @Override
    public void saveDEK(Address address, String nickname, UUID keyID, KeyStorage keyStorage) throws StorageException {
        LOGGER.debug("Saving DEK for {}@{} as {}", nickname, address, keyID);
        DEKs.add(new MemberKeyRecord(address, nickname, keyID, keyStorage));
        saveKeysJSON();
    }

    @Override
    public synchronized KeyStorage loadPersonalKey(Address address, String nickname) throws KeyStorageNotFoundException {
        return memberKeys.stream()
                .filter(k -> k.nickname.equals(nickname) && k.address.equals(address))
                .map(k -> k.keyStorage)
                .findFirst()
                .orElseThrow(() -> new KeyStorageNotFoundException("address: " + address + "; nickname: " + nickname));
    }

    @Override
    public KeyStorage loadEncryptor(Address address, String nickname) throws KeyStorageNotFoundException {
        return memberKeys.stream()
                .filter(k -> Objects.equals(k.nickname, nickname) && k.address.equals(address))
                .map(k -> k.keyStorage)
                .findFirst()
                .orElseThrow(() -> new KeyStorageNotFoundException("address: " + address + "; nickname: " + nickname));
    }

    @Override
    public KeyEntry loadDEK(Address address, String nickname) throws KeyStorageNotFoundException {
        return DEKs.stream()
                .filter(k -> Objects.equals(k.nickname, nickname) && k.address.equals(address))
                .map(k -> new KeyEntry(k.keyID, k.keyStorage))
                .findFirst()
                .orElseThrow(() -> new KeyStorageNotFoundException("address: " + address + "; nickname: " + nickname));
    }

    @Override
    public KeyStorage loadDEK(Address address, UUID keyID) throws KeyStorageNotFoundException {
        return DEKs.stream()
                .filter(k -> k.keyID.equals(keyID) && k.address.equals(address))
                .map(k -> k.keyStorage)
                .findFirst()
                .orElseThrow(() -> new KeyStorageNotFoundException("address: " + address + "; keyID: " + keyID));
    }
}