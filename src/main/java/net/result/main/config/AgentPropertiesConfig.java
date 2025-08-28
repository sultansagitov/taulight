package net.result.main.config;

import net.result.sandnode.config.AgentConfig;
import net.result.sandnode.config.KeyEntry;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.ConfigurationException;
import net.result.sandnode.exception.FSException;
import net.result.sandnode.exception.ImpossibleRuntimeException;
import net.result.sandnode.exception.StorageException;
import net.result.sandnode.exception.crypto.*;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.util.Address;
import net.result.sandnode.util.FileUtil;
import net.result.sandnode.util.Member;
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
import java.util.Objects;
import java.util.Properties;
import java.util.UUID;

public class AgentPropertiesConfig implements AgentConfig {
    private static final Logger LOGGER = LogManager.getLogger(AgentPropertiesConfig.class);
    private final Path KEYS_PATH;

    private final Path SERVER_KEYS_JSON_PATH;
    private final Path MEMBER_KEYS_JSON_PATH;
    private final Path DEKS_JSON_PATH;

    public AgentPropertiesConfig() {
        this("taulight.properties");
    }

    public AgentPropertiesConfig(@NotNull String fileName) {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            properties.load(input);
        } catch (IOException e) {
            throw new ConfigurationException("Error when reading \"%s\"".formatted(fileName), e);
        }

        KEYS_PATH = FileUtil.resolveHome(properties.getProperty("agent.keys.dir_path"));

        if (KEYS_PATH.toFile().mkdirs())
            LOGGER.info("Directory created: {}", KEYS_PATH);

        SERVER_KEYS_JSON_PATH = KEYS_PATH.resolve("server-keys.json");
        MEMBER_KEYS_JSON_PATH = KEYS_PATH.resolve("member-keys.json");
        DEKS_JSON_PATH = KEYS_PATH.resolve("deks.json");

        ensureFile(SERVER_KEYS_JSON_PATH, "server-keys");
        ensureFile(MEMBER_KEYS_JSON_PATH, "member-keys");
        ensureFile(DEKS_JSON_PATH, "deks");
    }

    private void ensureFile(Path path, String arrayName) {
        try {
            if (!Files.exists(path)) {
                FileUtil.createFile(path);
                saveJsonArray(path, arrayName, new JSONArray());
            }
        } catch (NoSuchEncryptionException | CreatingKeyException | EncryptionTypeException e) {
            throw new StorageException(e);
        }
    }

    private JSONArray readArray(Path path, String arrayName) {
        try {
            String data = FileUtil.readString(path);
            return new JSONObject(data).getJSONArray(arrayName);
        } catch (Exception e) {
            throw new StorageException("Error reading " + arrayName, e);
        }
    }

    private void saveJsonArray(Path path, String arrayName, JSONArray arr) {
        JSONObject obj = new JSONObject().put(arrayName, arr);
        try (FileWriter fileWriter = new FileWriter(path.toFile())) {
            fileWriter.write(obj.toString());
        } catch (IOException e) {
            throw new StorageException(e);
        }
    }

    private boolean hasServerKey(@NotNull Address address) {
        return readArray(SERVER_KEYS_JSON_PATH, "server-keys").toList().stream()
                .map(o -> (JSONObject) o)
                .anyMatch(obj -> address.toString().equals(obj.getString("address")));
    }

    @Override
    public synchronized void saveServerKey(@NotNull Address address, @NotNull AsymmetricKeyStorage keyStorage) {
        String sanitizedAddress = address.toString().replaceAll("[.:\\\\/*?\"<>|]", "_");
        String filename = "%s_%s_public.key".formatted(sanitizedAddress, UUID.randomUUID());

        if (hasServerKey(address))
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

        JSONArray arr = readArray(SERVER_KEYS_JSON_PATH, "server-keys");
        arr.put(new KeyRecord(publicKeyPath, keyStorage, address, publicKeyString).toJSON());
        saveJsonArray(SERVER_KEYS_JSON_PATH, "server-keys", arr);
    }

    @Override
    public synchronized void savePersonalKey(Member member, KeyStorage keyStorage) {
        JSONArray arr = readArray(MEMBER_KEYS_JSON_PATH, "member-keys");
        arr.put(new MemberKeyRecord(member.address(), member.nickname(), keyStorage).toJSON());
        saveJsonArray(MEMBER_KEYS_JSON_PATH, "member-keys", arr);
    }

    @Override
    public void saveEncryptor(Member member, KeyStorage keyStorage) {
        JSONArray arr = readArray(MEMBER_KEYS_JSON_PATH, "member-keys");
        arr.put(new MemberKeyRecord(member.address(), member.nickname(), keyStorage).toJSON());
        saveJsonArray(MEMBER_KEYS_JSON_PATH, "member-keys", arr);
    }

    @Override
    public void saveDEK(Member member, UUID keyID, KeyStorage keyStorage) {
        JSONArray arr = readArray(DEKS_JSON_PATH, "deks");
        arr.put(new MemberKeyRecord(member.address(), member.nickname(), keyID, keyStorage).toJSON());
        saveJsonArray(DEKS_JSON_PATH, "deks", arr);
    }

    @Override
    public AsymmetricKeyStorage loadServerKey(@NotNull Address address) {
        for (Object o : readArray(SERVER_KEYS_JSON_PATH, "server-keys")) {
            JSONObject obj = (JSONObject) o;
            try {
                KeyRecord rec = KeyRecord.fromJSON(obj);
                if (rec.address.equals(address))
                    return rec.keyStorage.asymmetric();
            } catch (Exception e) {
                LOGGER.error("Invalid server key JSON", e);
            }
        }
        throw new KeyStorageNotFoundException(address.toString());
    }

    @Override
    public synchronized KeyStorage loadPersonalKey(Member member) {
        for (Object o : readArray(MEMBER_KEYS_JSON_PATH, "member-keys")) {
            JSONObject obj = (JSONObject) o;
            try {
                MemberKeyRecord rec = MemberKeyRecord.fromJSON(obj);
                if (rec.address.equals(member.address()) && rec.nickname.equals(member.nickname()))
                    return rec.keyStorage;
            } catch (Exception e) {
                LOGGER.error("Invalid member key JSON", e);
            }
        }
        throw new KeyStorageNotFoundException(member.toString());
    }

    @Override
    public KeyStorage loadEncryptor(Member member) {
        for (Object o : readArray(MEMBER_KEYS_JSON_PATH, "member-keys")) {
            JSONObject obj = (JSONObject) o;
            try {
                MemberKeyRecord rec = MemberKeyRecord.fromJSON(obj);
                if (rec.address.equals(member.address()) && Objects.equals(rec.nickname, member.nickname()))
                    return rec.keyStorage;
            } catch (Exception e) {
                LOGGER.error("Invalid encryptor JSON", e);
            }
        }
        throw new KeyStorageNotFoundException(member.toString());
    }

    @Override
    public KeyEntry loadDEK(Member member) {
        for (Object o : readArray(DEKS_JSON_PATH, "deks")) {
            JSONObject obj = (JSONObject) o;
            try {
                MemberKeyRecord rec = MemberKeyRecord.fromJSON(obj);
                if (rec.address.equals(member.address()) && Objects.equals(rec.nickname, member.nickname()))
                    return new KeyEntry(rec.keyID, rec.keyStorage);
            } catch (Exception e) {
                LOGGER.error("Invalid DEK JSON", e);
            }
        }
        throw new KeyStorageNotFoundException(member.toString());
    }

    @Override
    public KeyStorage loadDEK(Address address, UUID keyID) {
        for (Object o : readArray(DEKS_JSON_PATH, "deks")) {
            JSONObject obj = (JSONObject) o;
            try {
                MemberKeyRecord rec = MemberKeyRecord.fromJSON(obj);
                if (rec.address.equals(address) && rec.keyID.equals(keyID))
                    return rec.keyStorage;
            } catch (Exception e) {
                LOGGER.error("Invalid DEK JSON", e);
            }
        }
        throw new KeyStorageNotFoundException("address: " + address + "; keyID: " + keyID);
    }
}
