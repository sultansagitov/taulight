package net.result.sandnode.util.encryption.symmetric;

import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.symmetric.aes.AESKeyConvertor;
import net.result.sandnode.util.encryption.symmetric.interfaces.ISymmetricKeyConvertor;
import org.jetbrains.annotations.NotNull;

public class Symmetric {

    public static ISymmetricKeyConvertor getKeyConvertor(@NotNull Encryption encryption) throws CannotUseEncryption {
        return switch (encryption) {
            case AES -> AESKeyConvertor.instance();
            default -> throw new CannotUseEncryption("Can't find algorithm \"" + encryption + "\"");
        };
    }
}
