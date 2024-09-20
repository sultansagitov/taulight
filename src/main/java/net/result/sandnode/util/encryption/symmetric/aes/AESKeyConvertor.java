package net.result.sandnode.util.encryption.symmetric.aes;

import net.result.sandnode.util.encryption.symmetric.interfaces.ISymmetricKeyConvertor;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AESKeyConvertor implements ISymmetricKeyConvertor {
    public static @NotNull AESKeyStorage toKeyStorage(byte @NotNull [] aesKey) {
        final SecretKey secretKey = new SecretKeySpec(aesKey, 0, aesKey.length, "AES");
        return new AESKeyStorage().setKey(secretKey);
    }
}
