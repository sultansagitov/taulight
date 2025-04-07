package net.result.taulight.chain.sender;

import net.result.sandnode.chain.sender.ClientChain;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.util.IOController;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.message.types.MessageRequest;
import net.result.taulight.message.types.MessageResponse;

import java.util.List;
import java.util.UUID;

public class MessageClientChain extends ClientChain {
    private long count;
    private List<ChatMessageViewDTO> messages;

    public MessageClientChain(IOController io) {
        super(io);
    }

    public synchronized void getMessages(UUID chatID, int index, int size)
            throws InterruptedException, DeserializationException, ExpectedMessageException, SandnodeErrorException,
            UnknownSandnodeErrorException, UnprocessedMessagesException {
        send(new MessageRequest(chatID, index, size));
        RawMessage raw = queue.take();

        ServerErrorManager.instance().handleError(raw);

        MessageResponse response = new MessageResponse(raw);
        count = response.getCount();
        messages = response.getMessages();
    }

    public long getCount() {
        return count;
    }

    public List<ChatMessageViewDTO> getMessages() {
        return messages;
    }
}
