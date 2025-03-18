package net.result.taulight.chain.client;

import net.result.sandnode.chain.client.ClientChain;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.util.IOController;
import net.result.taulight.db.ServerChatMessage;
import net.result.taulight.message.types.MessageRequest;
import net.result.taulight.message.types.MessageResponse;

import java.util.List;
import java.util.UUID;

public class MessageClientChain extends ClientChain {
    private long count;
    private List<ServerChatMessage> messages;

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

    public List<ServerChatMessage> getMessages() {
        return messages;
    }
}
