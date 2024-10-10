package net.result.sandnode.util.encryption.symmetric;

import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.symmetric.aes.AESKeyConvertor;
import net.result.sandnode.util.encryption.symmetric.interfaces.ISymmetricKeyConvertor;
import org.jetbrains.annotations.NotNull;

import java.security.NoSuchAlgorithmException;

public class Symmetric {

    public static ISymmetricKeyConvertor getKeyConvertor(@NotNull Encryption encryption) throws NoSuchAlgorithmException {
        return switch (encryption) {
            case AES -> AESKeyConvertor.getInstance();
            default -> throw new NoSuchAlgorithmException("Can't find algorithm \"" + encryption + "\"");
        };
    }
}
