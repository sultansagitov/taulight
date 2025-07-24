package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.exception.ProtocolException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.UUIDMessage;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.message.types.ForwardRequest;

import java.util.UUID;

public class ForwardRequestClientChain extends ClientChain {
    public ForwardRequestClientChain(SandnodeClient client) {
        super(client);
    }

    public synchronized UUID message(ChatMessageInputDTO input)
            throws InterruptedException, ProtocolException, SandnodeErrorException {
        RawMessage uuidRaw = sendAndReceive(new ForwardRequest(input));
        uuidRaw.expect(MessageTypes.HAPPY);
        UUID uuid = new UUIDMessage(uuidRaw).uuid;
        receive().expect(MessageTypes.HAPPY);
        return uuid;
    }
}
