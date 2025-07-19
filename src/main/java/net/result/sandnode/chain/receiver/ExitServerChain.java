package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ExitMessage;
import net.result.sandnode.serverclient.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class ExitServerChain extends ServerChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(ExitServerChain.class);

    public ExitServerChain(Session session) {
        super(session);
    }

    @Override
    public @Nullable Message handle(RawMessage raw) throws Exception {
        new ExitMessage(raw);
        session.io.disconnect(false);
        session.server.removeSession(session);
        session.close();
        LOGGER.info("Client disconnected");

        return null;
    }
}
