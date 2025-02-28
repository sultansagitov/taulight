package net.result.taulight.chain.client;

import net.result.sandnode.chain.client.ClientChain;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.util.IOController;
import net.result.taulight.db.ServerChatMessage;
import net.result.taulight.message.types.MessageRequest;
import net.result.taulight.message.types.MessageResponse;

import java.util.List;
import java.util.UUID;

public class MessageClientChain extends ClientChain {
    private final UUID chatID;
    private final int index;
    private final int size;

    private List<ServerChatMessage> messages;

    public MessageClientChain(IOController io, UUID chatID, int index, int size) {
        super(io);
        this.chatID = chatID;
        this.index = index;
        this.size = size;
    }

    @Override
    public void sync() throws InterruptedException, DeserializationException, ExpectedMessageException,
            SandnodeErrorException, UnknownSandnodeErrorException {
        send(new MessageRequest(chatID, index, size));
        RawMessage raw = queue.take();

        if (raw.headers().type() == MessageTypes.ERR) {
            ErrorMessage errorMessage = new ErrorMessage(raw);
            ServerErrorManager.instance().throwAll(errorMessage.error);
        }

        MessageResponse response = new MessageResponse(raw);
        messages = response.getMessages();
    }

    public List<ServerChatMessage> getMessages() {
        return messages;
    }
}
