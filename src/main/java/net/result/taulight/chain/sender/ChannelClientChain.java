package net.result.taulight.chain.sender;

import net.result.sandnode.chain.sender.ClientChain;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.TextMessage;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.util.IOController;
import net.result.taulight.dto.CodeDTO;
import net.result.taulight.message.CodeListMessage;
import net.result.taulight.message.TauMessageTypes;
import net.result.taulight.message.types.ChannelRequest;
import net.result.sandnode.message.UUIDMessage;

import java.time.Duration;
import java.util.Collection;
import java.util.UUID;

public class ChannelClientChain extends ClientChain {
    public ChannelClientChain(IOController io) {
        super(io);
    }

    public synchronized UUID sendNewChannelRequest(String title)
            throws InterruptedException, UnknownSandnodeErrorException, SandnodeErrorException,
            DeserializationException, UnprocessedMessagesException {
        send(ChannelRequest.newChannel(title));
        RawMessage raw = queue.take();

        ServerErrorManager.instance().handleError(raw);

        return new UUIDMessage(raw).uuid;
    }

    public synchronized void sendLeaveRequest(UUID chatID) throws InterruptedException, ExpectedMessageException,
            SandnodeErrorException, UnknownSandnodeErrorException, UnprocessedMessagesException {
        send(ChannelRequest.leave(chatID));
        RawMessage raw = queue.take();

        ServerErrorManager.instance().handleError(raw);

        new HappyMessage(raw);
    }

    public synchronized String createInviteCode(UUID chatID, String otherNickname, Duration expirationTime)
            throws InterruptedException, SandnodeErrorException, UnknownSandnodeErrorException,
            UnprocessedMessagesException {
        send(ChannelRequest.addMember(chatID, otherNickname, expirationTime));
        RawMessage raw = queue.take();
        ServerErrorManager.instance().handleError(raw);

        return new TextMessage(raw).content();
    }

    public synchronized Collection<CodeDTO> getChannelCodes(UUID chatID)
            throws InterruptedException, UnknownSandnodeErrorException, SandnodeErrorException,
            UnprocessedMessagesException, DeserializationException, ExpectedMessageException {
        send(ChannelRequest.channelCodes(chatID));
        RawMessage raw = queue.take();
        ServerErrorManager.instance().handleError(raw);

        raw.expect(TauMessageTypes.CHANNEL);
        return new CodeListMessage(raw).codes();
    }

    public synchronized Collection<CodeDTO> getMyCodes() throws InterruptedException,
            UnknownSandnodeErrorException, SandnodeErrorException, DeserializationException,
            UnprocessedMessagesException, ExpectedMessageException {
        send(ChannelRequest.myCodes());
        RawMessage raw = queue.take();
        ServerErrorManager.instance().handleError(raw);

        raw.expect(TauMessageTypes.CHANNEL);
        return new CodeListMessage(raw).codes();
    }
}
