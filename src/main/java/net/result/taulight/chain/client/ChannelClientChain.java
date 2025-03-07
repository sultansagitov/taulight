package net.result.taulight.chain.client;

import net.result.sandnode.chain.client.ClientChain;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.util.IOController;
import net.result.taulight.message.types.ChannelRequest;
import net.result.taulight.message.types.UUIDMessage;

import java.util.UUID;

public class ChannelClientChain extends ClientChain {
    public ChannelClientChain(IOController io) {
        super(io);
    }

    public synchronized UUID sendNewChannelRequest(String title) throws InterruptedException, ExpectedMessageException,
            UnknownSandnodeErrorException, SandnodeErrorException, DeserializationException,
            UnprocessedMessagesException {
        send(ChannelRequest.newChannel(title));
        RawMessage raw = queue.take();

        if (raw.headers().type() == MessageTypes.ERR) {
            ErrorMessage errorMessage = new ErrorMessage(raw);
            ServerErrorManager.instance().throwAll(errorMessage.error);
        }

        return new UUIDMessage(raw).uuid;
    }

    public synchronized void sendLeaveRequest(UUID chatID) throws InterruptedException, ExpectedMessageException,
            SandnodeErrorException, UnknownSandnodeErrorException, UnprocessedMessagesException {
        send(ChannelRequest.leave(chatID));
        RawMessage raw = queue.take();

        if (raw.headers().type() == MessageTypes.ERR) {
            ErrorMessage errorMessage = new ErrorMessage(raw);
            ServerErrorManager.instance().throwAll(errorMessage.error);
        }

        new HappyMessage(raw);
    }

    public synchronized void sendAddMemberRequest(UUID chatID, String otherMemberID)
            throws InterruptedException, SandnodeErrorException, ExpectedMessageException,
            UnknownSandnodeErrorException, UnprocessedMessagesException {
        send(ChannelRequest.addMember(chatID, otherMemberID));
        RawMessage raw = queue.take();
        if (raw.headers().type() == MessageTypes.ERR) {
            ErrorMessage errorMessage = new ErrorMessage(raw);
            ServerErrorManager.instance().throwAll(errorMessage.error);
        }

        new HappyMessage(raw);
    }
}
