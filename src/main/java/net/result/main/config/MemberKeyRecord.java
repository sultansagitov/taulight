package net.result.main.config;

import net.result.sandnode.encryption.interfaces.*;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.exception.ImpossibleRuntimeException;
import net.result.sandnode.exception.crypto.CannotUseEncryption;
import net.result.sandnode.exception.crypto.CreatingKeyException;
import net.result.sandnode.exception.crypto.EncryptionTypeException;
import net.result.sandnode.exception.crypto.NoSuchEncryptionException;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Base64;
import java.util.UUID;

public record MemberKeyRecord(UUID keyID, KeyStorage keyStorage) {

    public JSONObject toJSON() {
        try {
            if (keyStorage instanceof AsymmetricKeyStorage a) {
                JSONObject json = new JSONObject()
                        .put("id", keyID)
                        .put("encryption", keyStorage.encryption().name())
                        .put("public", a.encodedPublicKey());
                try {
                    return json.put("private", a.encodedPrivateKey());
                } catch (Exception e) {
                    return json;
                }
            } else if (keyStorage instanceof SymmetricKeyStorage s) {
                return new JSONObject()
                        .put("id", keyID)
                        .put("encryption", keyStorage.encryption().name())
                        .put("encoded", s.encoded());
            } else {
                throw new ImpossibleRuntimeException("keyStorage is not asymmetric nor symmetric");
            }
        } catch (CannotUseEncryption e) {
            throw new RuntimeException(e);
        }
    }

    public static MemberKeyRecord fromJSON(JSONObject json)
            throws NoSuchEncryptionException, CreatingKeyException, EncryptionTypeException {
        UUID keyID = UUID.fromString(json.getString("id"));
        String encryptionType = json.getString("encryption");
        Encryption encryption = EncryptionManager.find(encryptionType).asymmetric();
        if (encryption.isAsymmetric()) {
            String publicString = json.getString("public");
            String privateString = null;
            try {
                privateString = json.getString("private");
            } catch (JSONException ignored) {
            }

            AsymmetricKeyStorage keyStorage;
            AsymmetricKeyStorage publicKey = encryption.asymmetric().publicKeyConvertor().toKeyStorage(publicString);
            if (privateString != null) {
                AsymmetricKeyStorage privateKey = encryption.asymmetric()
                        .privateKeyConvertor()
                        .toKeyStorage(privateString);
                keyStorage = encryption.asymmetric().merge(publicKey, privateKey);
            } else {
                keyStorage = publicKey;
            }

            return new MemberKeyRecord(keyID, keyStorage);
        } else {
            byte[] decoded = Base64.getDecoder().decode(json.getString("encoded"));
            SymmetricKeyStorage keyStorage = encryption.symmetric().toKeyStorage(decoded);
            return new MemberKeyRecord(keyID, keyStorage);
        }
    }
}
