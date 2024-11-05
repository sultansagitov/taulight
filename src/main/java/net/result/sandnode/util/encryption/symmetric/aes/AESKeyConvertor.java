package net.result.sandnode.util.encryption.symmetric.aes;

import net.result.sandnode.util.encryption.core.aes.AESKeyStorage;
import net.result.sandnode.util.encryption.symmetric.interfaces.ISymmetricKeyConvertor;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AESKeyConvertor implements ISymmetricKeyConvertor {

    private static final AESKeyConvertor INSTANCE = new AESKeyConvertor();

    public static AESKeyConvertor instance() {
        return INSTANCE;
    }

    @Override
    public @NotNull AESKeyStorage toKeyStorage(byte @NotNull [] bytes) {
        SecretKey secretKey = new SecretKeySpec(bytes, 0, bytes.length, "AES");
        return new AESKeyStorage(secretKey);
    }
}
