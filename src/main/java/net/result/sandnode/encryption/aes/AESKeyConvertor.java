package net.result.sandnode.encryption.aes;

import net.result.sandnode.encryption.interfaces.SymmetricKeyStorage;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AESKeyConvertor {
    public static byte @NotNull [] toBytes(SymmetricKeyStorage keyStorage) {
        AESKeyStorage aesKeyStorage = (AESKeyStorage) keyStorage;
        return aesKeyStorage.key().getEncoded();
    }

    public static @NotNull AESKeyStorage toKeyStorage(byte @NotNull [] bytes) {
        SecretKey secretKey = new SecretKeySpec(bytes, 0, bytes.length, "AES");
        return new AESKeyStorage(secretKey);
    }
}
