package net.result.taulight.chain.client;

import net.result.sandnode.chain.client.ClientChain;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.util.IOController;
import net.result.taulight.message.MemberRecord;
import net.result.taulight.message.TauMessageTypes;
import net.result.taulight.message.types.MembersResponse;
import net.result.taulight.message.types.UUIDMessage;

import java.util.Collection;
import java.util.UUID;

public class MembersClientChain extends ClientChain {
    public MembersClientChain(IOController io) {
        super(io);
    }

    public synchronized Collection<MemberRecord> getMembers(UUID chatID)
            throws InterruptedException, ExpectedMessageException, DeserializationException, SandnodeErrorException,
            UnknownSandnodeErrorException, UnprocessedMessagesException {
        send(new UUIDMessage(new Headers().setType(TauMessageTypes.MEMBERS), chatID));

        RawMessage raw = queue.take();

        if (raw.headers().type() == MessageTypes.ERR) {
            ErrorMessage errorMessage = new ErrorMessage(raw);
            ServerErrorManager.instance().throwAll(errorMessage.error);
        }

        return new MembersResponse(raw).getMembers();
    }
}
