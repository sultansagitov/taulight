package net.result.sandnode.encryption.aes;

import net.result.sandnode.encryption.interfaces.ISymmetricKeyConvertor;
import net.result.sandnode.encryption.interfaces.ISymmetricKeyStorage;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AESKeyConvertor implements ISymmetricKeyConvertor {

    private static final AESKeyConvertor INSTANCE = new AESKeyConvertor();

    public static AESKeyConvertor instance() {
        return INSTANCE;
    }

    @Override
    public byte @NotNull [] toBytes(ISymmetricKeyStorage keyStorage) {
        AESKeyStorage aesKeyStorage = (AESKeyStorage) keyStorage;
        return aesKeyStorage.key().getEncoded();
    }

    @Override
    public @NotNull AESKeyStorage toKeyStorage(byte @NotNull [] bytes) {
        SecretKey secretKey = new SecretKeySpec(bytes, 0, bytes.length, "AES");
        return new AESKeyStorage(secretKey);
    }
}
