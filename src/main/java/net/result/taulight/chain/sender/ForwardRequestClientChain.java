package net.result.taulight.chain.sender;

import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.chain.ClientChain;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.sandnode.exception.*;
import net.result.taulight.message.types.ForwardRequest;
import net.result.sandnode.message.UUIDMessage;

import java.util.*;

public class ForwardRequestClientChain extends ClientChain {
    public ForwardRequestClientChain(SandnodeClient client) {
        super(client);
    }

    public synchronized UUID message(ChatMessageInputDTO input)
            throws InterruptedException, DeserializationException, ExpectedMessageException,
            UnknownSandnodeErrorException, UnprocessedMessagesException, SandnodeErrorException {
        send(new ForwardRequest(input));

        RawMessage uuidRaw = queue.take();
        ServerErrorManager.instance().handleError(uuidRaw);
        uuidRaw.expect(MessageTypes.HAPPY);
        UUID uuid = new UUIDMessage(uuidRaw).uuid;

        RawMessage happyRaw = queue.take();
        ServerErrorManager.instance().handleError(happyRaw);
        new HappyMessage(happyRaw);

        return uuid;
    }
}
