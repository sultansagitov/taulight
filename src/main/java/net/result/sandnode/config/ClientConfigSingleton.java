package net.result.sandnode.config;

import net.result.sandnode.exceptions.ConfigurationException;
import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.EncryptionFactory;
import net.result.sandnode.util.encryption.asymmetric.AsymmetricEncryptionFactory;
import net.result.sandnode.util.encryption.asymmetric.AsymmetricKeyStorage;
import net.result.sandnode.util.encryption.asymmetric.interfaces.IAsymmetricConvertor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ini4j.Ini;
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

public class ClientConfigSingleton {
    private static final Logger LOGGER = LogManager.getLogger(ClientConfigSingleton.class);
    private static final ClientConfigSingleton instance;

    static {
        try {
            instance = new ClientConfigSingleton("client.ini");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final JSONObject KEYS_JSON;
    private final String INI_FILE;
    private final Path FOLDER_PATH;
    private final Path KEYS_PATH;
    private final Path KEYS_JSON_PATH;

    private ClientConfigSingleton(@NotNull String fileName) throws IOException {
        INI_FILE = fileName;

        final Ini ini;
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) throw new IOException("Unable to find " + fileName);

            ini = new Ini();
            ini.load(input);
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }

        FOLDER_PATH = PathUtil.resolveHomeInPath(ini.get("Client", "dir_path"));
        KEYS_PATH = Paths.get(FOLDER_PATH.toString(), ini.get("Keys", "dir_path"));
        if (KEYS_PATH.toFile().mkdirs()) {
            LOGGER.info("Directory created: {}", KEYS_PATH);
        }
        KEYS_JSON_PATH = Paths.get(KEYS_PATH.toString(), ini.get("Keys", "json_file_name"));
        if (Files.exists(KEYS_JSON_PATH)) {
            if (!Files.isDirectory(KEYS_JSON_PATH)) {
                KEYS_JSON = new JSONObject(new String(Files.readAllBytes(KEYS_JSON_PATH)));
            } else {
                LOGGER.error("\"{}\" is directory", KEYS_JSON_PATH);
                throw new FileSystemException("\"%s\" is directory".formatted(KEYS_JSON_PATH));
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

    public static ClientConfigSingleton getInstance() throws IOException {
        return instance;
    }

    public static void saveKeysJSON() throws IOException {
        try (FileWriter fileWriter = new FileWriter(instance.KEYS_JSON_PATH.toFile())) {
            fileWriter.write(instance.KEYS_JSON.toString());
        }
    }

    public static String getIniFile() {
        return instance.INI_FILE;
    }

    public static Path getFolderPath() {
        return instance.FOLDER_PATH;
    }

    public static Path getKeysPath() {
        return instance.KEYS_PATH;
    }

    public static JSONObject getKeysJSON() {
        return instance.KEYS_JSON;
    }

    public static @Nullable AsymmetricKeyStorage getPublicKey(String host, int port) throws IOException, CannotUseEncryption, NoSuchEncryptionException, CreatingKeyException {
        for (Object o : instance.KEYS_JSON.getJSONArray("keys")) {
            JSONObject keyObject = (JSONObject) o;
            if (keyObject.getString("host").equalsIgnoreCase(host) && (keyObject.getInt("port") == port)) {
                String encryptionString = keyObject.getString("encryption");
                Encryption encryption = EncryptionFactory.getEncryption(encryptionString);

                String path = keyObject.getString("path");

                String string = Files.readString(Path.of(path));
                IAsymmetricConvertor publicConvertor = AsymmetricEncryptionFactory.getPublicConvertor(encryption);

                return publicConvertor.toKeyStorage(string);
            }
        }

        return null;
    }

}
