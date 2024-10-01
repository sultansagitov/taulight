package net.result.sandnode.util.encryption.symmetric.aes;

import net.result.sandnode.util.encryption.symmetric.interfaces.ISymmetricKeyConvertor;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AESKeyConvertor implements ISymmetricKeyConvertor {

    private static final AESKeyConvertor instance = new AESKeyConvertor();

    private AESKeyConvertor() {
    }

    public @NotNull AESKeyStorage toKeyStorage(byte @NotNull [] aesKey) {
        SecretKey secretKey = new SecretKeySpec(aesKey, 0, aesKey.length, "AES");
        return AESKeyStorage.getInstance().setKey(secretKey);
    }

    public static AESKeyConvertor getInstance() {
        return instance;
    }
}
