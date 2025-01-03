package net.result.sandnode.chain.server;

import net.result.sandnode.exceptions.DataNotEncryptedException;
import net.result.sandnode.exceptions.EncryptionTypeException;
import net.result.sandnode.exceptions.ExpectedMessageException;
import net.result.sandnode.exceptions.NoSuchEncryptionException;
import net.result.sandnode.messages.types.HappyMessage;
import net.result.sandnode.messages.types.SymMessage;
import net.result.sandnode.server.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SymKeyServerChain extends ServerChain {
    private static final Logger LOGGER = LogManager.getLogger(SymKeyServerChain.class);

    public SymKeyServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, EncryptionTypeException, NoSuchEncryptionException,
            ExpectedMessageException, DataNotEncryptedException {
        SymMessage message = new SymMessage(queue.take());
        session.io.setClientKey(message.symmetricKeyStorage);

        send(new HappyMessage());

        LOGGER.info("Symmetric key initialized");
    }
}
