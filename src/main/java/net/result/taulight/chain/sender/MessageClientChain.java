package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.dto.PaginatedDTO;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.util.IOController;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.message.types.MessageRequest;
import net.result.taulight.message.types.MessageResponse;

import java.util.UUID;

public class MessageClientChain extends ClientChain {
    public MessageClientChain(IOController io) {
        super(io);
    }

    public synchronized PaginatedDTO<ChatMessageViewDTO> getMessages(UUID chatID, int index, int size)
            throws InterruptedException, DeserializationException, ExpectedMessageException, SandnodeErrorException,
            UnknownSandnodeErrorException, UnprocessedMessagesException {
        send(new MessageRequest(chatID, index, size));

        RawMessage raw = queue.take();
        ServerErrorManager.instance().handleError(raw);

        return new MessageResponse(raw).getPaginated();
    }
}
