package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.exception.ProtocolException;
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
            throws InterruptedException, ProtocolException, SandnodeErrorException {
        var raw = sendAndReceive(new UUIDMessage(new Headers().setType(TauMessageTypes.MEMBERS), chatID));
        return new MembersResponse(raw).dto();
    }
}
