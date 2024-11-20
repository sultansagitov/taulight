package net.result.sandnode.util.encryption.aes;

import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.util.encryption.interfaces.ISymmetricKeyConvertor;
import net.result.sandnode.util.encryption.interfaces.ISymmetricKeyStorage;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AESKeyConvertor implements ISymmetricKeyConvertor {

    private static final AESKeyConvertor INSTANCE = new AESKeyConvertor();

    public static AESKeyConvertor instance() {
        return INSTANCE;
    }

    @Override
    public byte @NotNull [] toBytes(ISymmetricKeyStorage keyStorage) throws CannotUseEncryption {
        if (keyStorage instanceof AESKeyStorage) {
            AESKeyStorage aesKeyStorage = (AESKeyStorage) keyStorage;
            return aesKeyStorage.getKey().getEncoded();
        }
        throw new CannotUseEncryption(keyStorage.encryption());
    }

    @Override
    public @NotNull AESKeyStorage toKeyStorage(byte @NotNull [] bytes) {
        SecretKey secretKey = new SecretKeySpec(bytes, 0, bytes.length, "AES");
        return new AESKeyStorage(secretKey);
    }
}
