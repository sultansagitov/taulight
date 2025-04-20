package net.result.main.chain.sender;

import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.util.IOController;
import net.result.sandnode.chain.sender.ClientChain;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.sandnode.exception.*;
import net.result.taulight.message.types.ForwardRequest;
import net.result.sandnode.message.UUIDMessage;

import java.util.*;

public class ConsoleForwardRequestClientChain extends ClientChain {
    public ConsoleForwardRequestClientChain(IOController io) {
        super(io);
    }

    public synchronized UUID message(ChatMessageInputDTO input)
            throws InterruptedException, DeserializationException, ExpectedMessageException,
            UnknownSandnodeErrorException, UnprocessedMessagesException, SandnodeErrorException {
        send(new ForwardRequest(input));
        RawMessage raw = queue.take();
        ServerErrorManager.instance().handleError(raw);
        raw.expect(MessageTypes.HAPPY);
        return new UUIDMessage(raw).uuid;
    }
}
