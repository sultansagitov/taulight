package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.UUIDMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.dto.ChatMemberDTO;
import net.result.taulight.message.TauMessageTypes;
import net.result.taulight.message.types.MembersResponse;

import java.util.Collection;
import java.util.UUID;

public class MembersClientChain extends ClientChain {
    public MembersClientChain(SandnodeClient client) {
        super(client);
    }

    public synchronized Collection<ChatMemberDTO> getMembers(UUID chatID)
            throws InterruptedException, ExpectedMessageException, DeserializationException, SandnodeErrorException,
            UnknownSandnodeErrorException, UnprocessedMessagesException {
        send(new UUIDMessage(new Headers().setType(TauMessageTypes.MEMBERS), chatID));

        RawMessage raw = queue.take();

        ServerErrorManager.instance().handleError(raw);

        return new MembersResponse(raw).getMembers();
    }
}
