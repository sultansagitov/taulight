package net.result.sandnode.protocol;

import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.messages.util.MessageType;
import net.result.sandnode.util.encryption.Encryption;
import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.util.encryption.Encryption.*;

public class FromByte {
    public static @NotNull Encryption getEncryptionString(byte encryption) throws NoSuchEncryptionException {
        return switch (encryption) {
            case 0 -> NO;
            case 1 -> RSA;
            case 2 -> AES;
            default -> throw new NoSuchEncryptionException("No such encryption for " + encryption);
        };
    }

    public static byte getEncryptionByte(@NotNull Encryption encryption) {
        return switch (encryption) {
            case NO -> 0;
            case RSA -> 1;
            case AES -> 2;
        };
    }

    public static @NotNull MessageType getMessageType(byte type) {
        for (MessageType messageType : MessageType.values())
            if (messageType.getByte() == type)
                return messageType;
        throw new IllegalArgumentException("Unknown MessageType: " + type);
    }
}
