package net.result.main.config;

import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.encryption.interfaces.SymmetricEncryption;
import net.result.sandnode.encryption.interfaces.SymmetricKeyStorage;
import net.result.sandnode.util.Address;
import net.result.sandnode.util.Member;
import org.bouncycastle.util.encoders.Base64;
import org.json.JSONObject;

import java.util.UUID;

public record DEKRecord(Member m1, Member m2, UUID keyID, KeyStorage keyStorage) {

    public static DEKRecord fromJSON(JSONObject obj) {
        JSONObject m1Object = obj.getJSONObject("m1");
        JSONObject m2Object = obj.getJSONObject("m2");
        Member m1 = new Member(m1Object.getString("nickname"), Address.getFromString(m1Object.getString("address")));
        Member m2 = new Member(m2Object.getString("nickname"), Address.getFromString(m2Object.getString("address")));
        UUID keyID = UUID.fromString(obj.getString("key-id"));

        SymmetricEncryption encryption = EncryptionManager.find(obj.getString("key-encryption")).symmetric();

        SymmetricKeyStorage keyStorage = encryption.toKeyStorage(Base64.decode(obj.getString("key-encoded")));

        return new DEKRecord(m1, m2, keyID, keyStorage);
    }

    public JSONObject toJSON() {
        return new JSONObject()
                .put("m1", new JSONObject().put("nickname", m1.nickname()).put("address", m1.address()))
                .put("m2", new JSONObject().put("nickname", m2.nickname()).put("address", m2.address()))
                .put("key-id", keyID.toString())
                .put("key-encryption", keyStorage.encryption().toString())
                .put("key-encoded", ((SymmetricKeyStorage) keyStorage).encoded());
    }
}
