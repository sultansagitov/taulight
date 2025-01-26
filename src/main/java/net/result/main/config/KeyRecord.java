package net.result.main.config;

import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.IAsymmetricConvertor;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.exception.*;
import net.result.sandnode.hasher.HasherManager;
import net.result.sandnode.hasher.Hasher;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.nio.file.Path;

import static net.result.sandnode.hasher.Hashers.SHA256;

public final class KeyRecord {
    public final Path publicKeyPath;
    public final AsymmetricKeyStorage keyStorage;
    public final Hasher hasher = SHA256;
    public final String hash;
    public final Endpoint endpoint;

    public KeyRecord(Path publicKeyPath, AsymmetricKeyStorage keyStorage, Endpoint endpoint, String encodedKey) {
        this.publicKeyPath = publicKeyPath;
        this.keyStorage = keyStorage;
        this.endpoint = endpoint;
        this.hash = hasher.hash(encodedKey);
    }

    public static @NotNull KeyRecord fromJSON(@NotNull JSONObject json)
            throws NoSuchEncryptionException, CreatingKeyException, FSException, NoSuchHasherException,
            KeyHashCheckingSecurityException, EncryptionTypeException, InvalidEndpointSyntax {
        Path path = Path.of(json.getString("path"));
        var encryption = EncryptionManager.find(json.getString("encryption")).asymmetric();
        IAsymmetricConvertor convertor = encryption.publicKeyConvertor();
        AsymmetricKeyStorage keyStorage = convertor.toKeyStorage(FileUtil.readString(path));

        JSONObject hashObject = json.getJSONObject("hash");
        String hash1 = hashObject.getString("content");
        String encodedString;
        try {
            encodedString = keyStorage.encodedPublicKey();
        } catch (CannotUseEncryption e) {
            throw new ImpossibleRuntimeException(e);
        }
        Hasher hasher = HasherManager.find(hashObject.getString("algorithm"));
        String hash2 = hasher.hash(encodedString);

        if (!hash1.equals(hash2)) {
            throw new KeyHashCheckingSecurityException(hash1, hash2);
        }

        Endpoint fromString = Endpoint.getFromString(json.getString("endpoint"), 52525);
        return new KeyRecord(path, keyStorage, fromString, "");
    }

    public JSONObject toJSON() {
        return new JSONObject()
                .put("path", publicKeyPath.toString())
                .put("encryption", keyStorage.encryption().name())
                .put("hash", new JSONObject()
                        .put("algorithm", hasher.name())
                        .put("content", hash)
                )
                .put("endpoint", endpoint.toString());
    }
}
