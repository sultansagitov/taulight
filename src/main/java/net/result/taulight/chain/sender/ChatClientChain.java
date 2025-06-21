package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.dto.ChatInfoDTO;
import net.result.taulight.dto.ChatInfoPropDTO;
import net.result.taulight.message.types.ChatRequest;
import net.result.taulight.message.types.ChatResponse;

import java.util.Collection;
import java.util.UUID;

public class ChatClientChain extends ClientChain {
    public ChatClientChain(SandnodeClient client) {
        super(client);
    }

    public synchronized Collection<ChatInfoDTO> getByMember(Collection<ChatInfoPropDTO> infoProps)
            throws InterruptedException, DeserializationException, ExpectedMessageException, SandnodeErrorException,
            UnknownSandnodeErrorException, UnprocessedMessagesException {
        send(ChatRequest.getByMember(infoProps));
        RawMessage raw = queue.take();

        ServerErrorManager.instance().handleError(raw);

        return new ChatResponse(raw).getInfos();
    }

    public synchronized Collection<ChatInfoDTO> getByID(Collection<UUID> chatID, Collection<ChatInfoPropDTO> infoProps)
            throws InterruptedException, ExpectedMessageException, UnknownSandnodeErrorException,
            SandnodeErrorException, DeserializationException, UnprocessedMessagesException {
        send(ChatRequest.getByID(chatID, infoProps));
        RawMessage raw = queue.take();

        ServerErrorManager.instance().handleError(raw);

        return new ChatResponse(raw).getInfos();
    }
}
