package net.result.sandnode.server.commands;

import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.server.Session;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

import static net.result.sandnode.util.encryption.Encryption.NO;

public class MulticastResponseCommand extends ResponseCommand {
    private static final Logger LOGGER = LogManager.getLogger(MulticastResponseCommand.class);

    public MulticastResponseCommand(@NotNull IMessage response) {
        super(response);
    }

    @Override
    public void execute(
            @NotNull List<Session> sessionList,
            @NotNull Session session,
            @NotNull GlobalKeyStorage globalKeyStorage
    ) throws IOException, ReadingKeyException, EncryptionException {
        for (Session s : sessionList) {
            s.socket.getOutputStream().write(response.toByteArray(NO, globalKeyStorage));
            LOGGER.info("Message was sent: {}", response);
        }
    }
}
