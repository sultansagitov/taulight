package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.message.types.SymMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SymKeyServerChain extends ServerChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(SymKeyServerChain.class);

    @Override
    public HappyMessage handle(RawMessage raw) {
        SymMessage message = new SymMessage(raw);
        session.io().setClientKey(message.symmetricKeyStorage);
        LOGGER.info("Symmetric key initialized");

        return new HappyMessage();

    }
}
