package net.result.main.config;

import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.Encryption;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.encryption.interfaces.SymmetricKeyStorage;
import net.result.sandnode.exception.ImpossibleRuntimeException;
import net.result.sandnode.exception.InvalidAddressSyntax;
import net.result.sandnode.exception.crypto.CannotUseEncryption;
import net.result.sandnode.exception.crypto.CreatingKeyException;
import net.result.sandnode.exception.crypto.EncryptionTypeException;
import net.result.sandnode.exception.crypto.NoSuchEncryptionException;
import net.result.sandnode.util.Address;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Base64;
import java.util.UUID;

public class MemberKeyRecord {
    public final Address address;
    public final UUID keyID;
    public final KeyStorage keyStorage;
    public String nickname = null;

    public MemberKeyRecord(Address address, UUID keyID, KeyStorage keyStorage) {
        this.address = address;
        this.keyID = keyID;
        this.keyStorage = keyStorage;
    }

    public MemberKeyRecord(Address address, String nickname, UUID keyID, KeyStorage keyStorage) {
        this.address = address;
        this.nickname = nickname;
        this.keyID = keyID;
        this.keyStorage = keyStorage;
    }

    public JSONObject toJSON() {
        try {
            JSONObject json = new JSONObject()
                    .put("id", keyID)
                    .put("address", address.toString())
                    .put("encryption", keyStorage.encryption().name());

            if (nickname != null) {
                json.put("nickname", nickname);
            }

            if (keyStorage instanceof AsymmetricKeyStorage a) {
                json.put("public", a.encodedPublicKey());
                try {
                    return json.put("private", a.encodedPrivateKey());
                } catch (Exception e) {
                    return json;
                }
            } else if (keyStorage instanceof SymmetricKeyStorage s) {
                return json.put("encoded", s.encoded());
            } else {
                throw new ImpossibleRuntimeException("keyStorage is not asymmetric nor symmetric");
            }
        } catch (CannotUseEncryption e) {
            throw new RuntimeException(e);
        }
    }

    public static MemberKeyRecord fromJSON(JSONObject json)
            throws NoSuchEncryptionException, CreatingKeyException, EncryptionTypeException, InvalidAddressSyntax {
        MemberKeyRecord result;
        UUID keyID = UUID.fromString(json.getString("id"));
        String encryptionType = json.getString("encryption");
        Address address = Address.getFromString(json.getString("address"), 52525);
        Encryption encryption = EncryptionManager.find(encryptionType);
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

            result = new MemberKeyRecord(address, keyID, keyStorage);
        } else {
            byte[] decoded = Base64.getDecoder().decode(json.getString("encoded"));
            SymmetricKeyStorage keyStorage = encryption.symmetric().toKeyStorage(decoded);
            result = new MemberKeyRecord(address, keyID, keyStorage);
        }

        try {
            result.nickname = json.getString("nickname");
        } catch (Exception ignored) {}

        return result;
    }
}
