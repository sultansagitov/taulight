package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.crypto.DataNotEncryptedException;
import net.result.sandnode.exception.crypto.EncryptionTypeException;
import net.result.sandnode.exception.crypto.NoSuchEncryptionException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.message.types.SymMessage;
import net.result.sandnode.serverclient.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SymKeyServerChain extends ServerChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(SymKeyServerChain.class);

    public SymKeyServerChain(Session session) {
        super(session);
    }

    @Override
    public HappyMessage handle(RawMessage raw) throws EncryptionTypeException, NoSuchEncryptionException,
            ExpectedMessageException, DataNotEncryptedException, DeserializationException {
        SymMessage message = new SymMessage(raw);
        session.io().setClientKey(message.symmetricKeyStorage);
        LOGGER.info("Symmetric key initialized");

        return new HappyMessage();

    }
}
