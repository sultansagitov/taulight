package net.result.sandnode.util.encryption.aes;

import net.result.sandnode.util.encryption.symmetric.aes.AESKeyStorage;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AESKeyDecoder {

    public static @NotNull AESKeyStorage getKeyStore(byte[] bytes) {
        SecretKey aes = new SecretKeySpec(bytes, 0, bytes.length, "AES");
        return new AESKeyStorage().setKey(aes);
    }

}
