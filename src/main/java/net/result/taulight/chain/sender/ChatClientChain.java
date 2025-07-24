package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
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
            throws InterruptedException, ProtocolException, SandnodeErrorException {
        var raw = sendAndReceive(ChatRequest.getByMember(infoProps));
        return new ChatResponse(raw).getInfos();
    }

    public synchronized Collection<ChatInfoDTO> getByID(Collection<UUID> chatID, Collection<ChatInfoPropDTO> infoProps)
            throws InterruptedException, ProtocolException, SandnodeErrorException {
        var raw = sendAndReceive(ChatRequest.getByID(chatID, infoProps));
        return new ChatResponse(raw).getInfos();
    }
}
