package net.result.sandnode.server.handlers;

import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.messages.IMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public interface IProtocolHandler {

    @Nullable IMessage getResponse(@NotNull IMessage request) throws IOException, NoSuchAlgorithmException, ReadingKeyException, EncryptionException, DecryptionException;

}
