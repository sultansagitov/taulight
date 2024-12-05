package net.result.sandnode.messages;

import net.result.sandnode.exceptions.IllegalMessageLengthException;
import net.result.sandnode.exceptions.KeyStorageNotFoundException;
import net.result.sandnode.exceptions.EncryptionException;
import net.result.sandnode.exceptions.MessageSerializationException;
import net.result.sandnode.messages.util.Headers;
import net.result.sandnode.encryption.GlobalKeyStorage;
import net.result.sandnode.encryption.interfaces.IEncryption;
import org.jetbrains.annotations.NotNull;

public interface IMessage {

    Headers getHeaders();

    byte[] getBody();

    byte[] toByteArray(@NotNull GlobalKeyStorage globalKeyStorage, @NotNull IEncryption encryption)
            throws EncryptionException, KeyStorageNotFoundException, MessageSerializationException, IllegalMessageLengthException;
}
