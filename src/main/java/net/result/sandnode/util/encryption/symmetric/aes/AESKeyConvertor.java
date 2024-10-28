package net.result.sandnode.util.encryption.symmetric.aes;

import net.result.sandnode.util.encryption.symmetric.interfaces.ISymmetricKeyConvertor;
import org.jetbrains.annotations.NotNull;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AESKeyConvertor implements ISymmetricKeyConvertor {

    private static final AESKeyConvertor instance = new AESKeyConvertor();

    @Override
    public @NotNull AESKeyStorage toKeyStorage(byte @NotNull [] bytes) {
        SecretKey secretKey = new SecretKeySpec(bytes, 0, bytes.length, "AES");
        return AESKeyStorage.getInstance().setKey(secretKey);
    }

    public static AESKeyConvertor getInstance() {
        return instance;
    }
}
