package net.result.main.config;

import net.result.main.exception.crypto.KeyHashCheckingException;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.AsymmetricConvertor;
import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.exception.ImpossibleRuntimeException;
import net.result.sandnode.exception.crypto.CannotUseEncryption;
import net.result.sandnode.hasher.Hasher;
import net.result.sandnode.hasher.HasherManager;
import net.result.sandnode.hasher.Hashers;
import net.result.sandnode.util.Address;
import net.result.sandnode.util.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.nio.file.Path;

public final class KeyRecord {
    public final Path publicKeyPath;
    public final AsymmetricKeyStorage keyStorage;
    public final Hasher hasher = Hashers.SHA256;
    public final String hash;
    public final Address address;

    public KeyRecord(Path publicKeyPath, AsymmetricKeyStorage keyStorage, Address address, String encodedKey) {
        this.publicKeyPath = publicKeyPath;
        this.keyStorage = keyStorage;
        this.address = address;
        hash = hasher.hash(encodedKey);
    }

    public static @NotNull KeyRecord fromJSON(@NotNull JSONObject json) {
        Path path = Path.of(json.getString("path"));
        AsymmetricEncryption encryption = EncryptionManager.find(json.getString("encryption")).asymmetric();
        AsymmetricConvertor convertor = encryption.publicKeyConvertor();
        AsymmetricKeyStorage keyStorage = convertor.toKeyStorage(FileUtil.readString(path));

        JSONObject hashObject = json.getJSONObject("hash");
        Hasher hasher = HasherManager.find(hashObject.getString("algorithm"));
        String hash1 = hashObject.getString("content");
        String encodedString;
        try {
            encodedString = keyStorage.encodedPublicKey();
        } catch (CannotUseEncryption e) {
            throw new ImpossibleRuntimeException(e);
        }
        String hash2 = hasher.hash(encodedString);

        if (!hash1.equals(hash2)) {
            throw new KeyHashCheckingException(hash1, hash2);
        }

        Address fromString = Address.getFromString(json.getString("address"));
        return new KeyRecord(path, keyStorage, fromString, encodedString);
    }

    public JSONObject toJSON() {
        return new JSONObject()
                .put("path", publicKeyPath.toString())
                .put("encryption", keyStorage.encryption().name())
                .put("hash", new JSONObject()
                        .put("algorithm", hasher.name())
                        .put("content", hash)
                )
                .put("address", address.toString());
    }
}
