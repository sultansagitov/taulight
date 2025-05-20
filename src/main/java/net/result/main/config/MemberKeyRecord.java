package net.result.main.config;

import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.exception.crypto.CannotUseEncryption;
import net.result.sandnode.exception.crypto.CreatingKeyException;
import net.result.sandnode.exception.crypto.EncryptionTypeException;
import net.result.sandnode.exception.crypto.NoSuchEncryptionException;
import org.json.JSONObject;

import java.nio.file.Path;

public record MemberKeyRecord(Path keyPath, AsymmetricKeyStorage keyStorage) {

    public JSONObject toJSON() throws CannotUseEncryption {
        return new JSONObject()
                .put("encryption", keyStorage.encryption().name())
                .put("public", keyStorage.encodedPublicKey())
                .put("private", keyStorage.encodedPrivateKey());
    }

    public static MemberKeyRecord fromJSON(Path path, JSONObject json)
            throws NoSuchEncryptionException, CreatingKeyException, EncryptionTypeException {
        String encryptionType = json.getString("encryption");
        String publicString = json.getString("public");
        String privateString = json.getString("private");

        AsymmetricEncryption encryption = EncryptionManager.find(encryptionType).asymmetric();
        AsymmetricKeyStorage publicKey = encryption.publicKeyConvertor().toKeyStorage(publicString);
        AsymmetricKeyStorage privateKey = encryption.privateKeyConvertor().toKeyStorage(privateString);

        AsymmetricKeyStorage keyStorage = encryption.merge(publicKey, privateKey);

        return new MemberKeyRecord(path, keyStorage);
    }
}
