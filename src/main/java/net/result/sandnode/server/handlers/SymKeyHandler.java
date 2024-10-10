package net.result.sandnode.server.handlers;

import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.server.Session;
import net.result.sandnode.server.commands.ICommand;
import net.result.sandnode.server.commands.SymKeyCommand;
import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.NoSuchAlgorithmException;
import java.util.List;

public class SymKeyHandler implements IProtocolHandler {

    private static final SymKeyHandler instance = new SymKeyHandler();

    private SymKeyHandler() {}

    public static SymKeyHandler getInstance() {
        return instance;
    }

    @Override
    public @Nullable ICommand getCommand(@NotNull RawMessage request, @NotNull List<Session> sessionList, @NotNull Session session, @NotNull GlobalKeyStorage globalKeyStorage) throws NoSuchAlgorithmException, NoSuchEncryptionException {
        Encryption encryption = Encryption.fromByte(Byte.parseByte(request.getHeaders().get("encryption")));
        return new SymKeyCommand(encryption, request.getBody());
    }
}
