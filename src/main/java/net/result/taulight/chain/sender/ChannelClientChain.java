package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.dto.FileDTO;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.TextMessage;
import net.result.sandnode.message.UUIDMessage;
import net.result.sandnode.message.types.FileMessage;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.dto.CodeDTO;
import net.result.taulight.message.CodeListMessage;
import net.result.taulight.message.TauMessageTypes;
import net.result.taulight.message.types.ChannelRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collection;
import java.util.UUID;

public class ChannelClientChain extends ClientChain {
    private static final Logger LOGGER = LogManager.getLogger(ChannelClientChain.class);

    public ChannelClientChain(SandnodeClient client) {
        super(client);
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

    public synchronized void setAvatar(UUID chatID, String avatarPath) throws UnprocessedMessagesException, FSException,
            InterruptedException, UnknownSandnodeErrorException, SandnodeErrorException, ExpectedMessageException {

        Path path = Paths.get(avatarPath);

        String contentType = URLConnection.guessContentTypeFromName(path.getFileName().toString());
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(path);
        } catch (IOException e) {
            LOGGER.error("Failed to read file at path: {}", avatarPath, e);
            throw new FSException(e);
        }

        IMessage request = ChannelRequest.setAvatar(chatID);
        FileMessage fileMessage = new FileMessage(new FileDTO(contentType, bytes));

        send(request);
        send(fileMessage);

        RawMessage raw = queue.take();
        ServerErrorManager.instance().handleError(raw);

        new HappyMessage(raw);
    }

    public synchronized @Nullable FileDTO getAvatar(UUID chatID) throws UnprocessedMessagesException,
            InterruptedException, UnknownSandnodeErrorException, SandnodeErrorException, ExpectedMessageException {
        send(ChannelRequest.getAvatar(chatID));

        RawMessage raw = queue.take();
        try {
            ServerErrorManager.instance().handleError(raw);
        } catch (NoEffectException e) {
            return null;
        }

        return new FileMessage(raw).dto();
    }
}
