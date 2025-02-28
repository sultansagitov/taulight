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

import java.util.UUID;

public class ChannelClientChain extends ClientChain {
    public ChannelClientChain(IOController io) {
        super(io);
    }

    @Override
    public boolean isChainStartAllowed() {
        return false;
    }

    @Override
    public void sync() {
        throw new ImpossibleRuntimeException("This chain should not be started");
    }

    public void sendNewChannelRequest(String title) throws InterruptedException, ExpectedMessageException {
        send(ChannelRequest.newChannel(title));
        RawMessage raw = queue.take();
        new HappyMessage(raw);
    }

    public void sendLeaveRequest(UUID chatID) throws InterruptedException, ExpectedMessageException,
            SandnodeErrorException, UnknownSandnodeErrorException {
        send(ChannelRequest.leave(chatID));
        RawMessage raw = queue.take();

        if (raw.headers().type() == MessageTypes.ERR) {
            ErrorMessage errorMessage = new ErrorMessage(raw);
            ServerErrorManager.instance().throwAll(errorMessage.error);
        }

        new HappyMessage(raw);
    }

    public void sendAddMemberRequest(UUID chatID, String otherMemberID) throws InterruptedException,
            SandnodeErrorException, ExpectedMessageException, UnknownSandnodeErrorException {
        send(ChannelRequest.addMember(chatID, otherMemberID));
        RawMessage raw = queue.take();
        if (raw.headers().type() == MessageTypes.ERR) {
            ErrorMessage errorMessage = new ErrorMessage(raw);
            ServerErrorManager.instance().throwAll(errorMessage.error);
        }

        new HappyMessage(raw);
    }
}
