package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.UUIDMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.dto.MembersResponseDTO;
import net.result.taulight.message.TauMessageTypes;
import net.result.taulight.message.types.MembersResponse;

import java.util.UUID;

public class MembersClientChain extends ClientChain {
    public MembersClientChain(SandnodeClient client) {
        super(client);
    }

    public synchronized MembersResponseDTO getMembers(UUID chatID)
            throws InterruptedException, ExpectedMessageException, DeserializationException, SandnodeErrorException,
            UnknownSandnodeErrorException, UnprocessedMessagesException {
        send(new UUIDMessage(new Headers().setType(TauMessageTypes.MEMBERS), chatID));
        return new MembersResponse(receive()).dto();
    }
}
