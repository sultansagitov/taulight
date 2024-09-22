package net.result.sandnode.server.commands;

import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.server.Session;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface ICommand {
    void execute(List<Session> sessionList, Session session, @NotNull GlobalKeyStorage globalKeyStorage) throws IOException, NoSuchEncryptionException, ReadingKeyException, EncryptionException, NoSuchAlgorithmException;
}
