package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.dto.FileDTO;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.TextMessage;
import net.result.sandnode.message.UUIDMessage;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.util.FileIOUtil;
import net.result.taulight.dto.CodeDTO;
import net.result.taulight.message.CodeListMessage;
import net.result.taulight.message.TauMessageTypes;
import net.result.taulight.message.types.GroupRequest;
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

public class GroupClientChain extends ClientChain {
    private static final Logger LOGGER = LogManager.getLogger(GroupClientChain.class);

    public GroupClientChain(SandnodeClient client) {
        super(client);
    }

    public synchronized UUID sendNewGroupRequest(String title)
            throws InterruptedException, UnknownSandnodeErrorException, SandnodeErrorException,
            DeserializationException, UnprocessedMessagesException {
        send(GroupRequest.newGroup(title));
        RawMessage raw = receive();

        ServerErrorManager.instance().handleError(raw);

        return new UUIDMessage(raw).uuid;
    }

    public synchronized void sendLeaveRequest(UUID chatID) throws InterruptedException, ExpectedMessageException,
            SandnodeErrorException, UnknownSandnodeErrorException, UnprocessedMessagesException {
        send(GroupRequest.leave(chatID));
        RawMessage raw = receive();

        ServerErrorManager.instance().handleError(raw);

        new HappyMessage(raw);
    }

    public synchronized String createInviteCode(UUID chatID, String otherNickname, Duration expirationTime)
            throws InterruptedException, SandnodeErrorException, UnknownSandnodeErrorException,
            UnprocessedMessagesException {
        return createInviteCode(chatID, otherNickname, String.valueOf(expirationTime.toSeconds()));
    }

    public synchronized String createInviteCode(UUID chatID, String otherNickname, String expirationTime)
            throws InterruptedException, SandnodeErrorException, UnknownSandnodeErrorException,
            UnprocessedMessagesException {
        send(GroupRequest.addMember(chatID, otherNickname, expirationTime));
        RawMessage raw = receive();
        ServerErrorManager.instance().handleError(raw);

        return new TextMessage(raw).content();
    }

    public synchronized Collection<CodeDTO> getGroupCodes(UUID chatID)
            throws InterruptedException, UnknownSandnodeErrorException, SandnodeErrorException,
            UnprocessedMessagesException, DeserializationException, ExpectedMessageException {
        send(GroupRequest.groupCodes(chatID));
        RawMessage raw = receive();
        ServerErrorManager.instance().handleError(raw);

        raw.expect(TauMessageTypes.GROUP);
        return new CodeListMessage(raw).codes();
    }

    public synchronized Collection<CodeDTO> getMyCodes() throws InterruptedException,
            UnknownSandnodeErrorException, SandnodeErrorException, DeserializationException,
            UnprocessedMessagesException, ExpectedMessageException {
        send(GroupRequest.myCodes());
        RawMessage raw = receive();
        ServerErrorManager.instance().handleError(raw);

        raw.expect(TauMessageTypes.GROUP);
        return new CodeListMessage(raw).codes();
    }

    public synchronized UUID setAvatar(UUID chatID, String avatarPath)
            throws UnprocessedMessagesException, FSException, InterruptedException, UnknownSandnodeErrorException,
            SandnodeErrorException, ExpectedMessageException, DeserializationException {

        Path path = Paths.get(avatarPath);

        String contentType = URLConnection.guessContentTypeFromName(path.getFileName().toString());
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(path);
        } catch (IOException e) {
            LOGGER.error("Failed to read file at path: {}", avatarPath, e);
            throw new FSException(e);
        }

        Message request = GroupRequest.setAvatar(chatID);
        FileDTO dto = new FileDTO(null, contentType, bytes);

        send(request);
        FileIOUtil.send(dto, this::send);

        RawMessage raw = receive();
        ServerErrorManager.instance().handleError(raw);

        raw.expect(MessageTypes.HAPPY);
        UUIDMessage response = new UUIDMessage(raw);

        return response.uuid;
    }

    public synchronized @Nullable FileDTO getAvatar(UUID chatID) throws UnprocessedMessagesException,
            InterruptedException, UnknownSandnodeErrorException, SandnodeErrorException, ExpectedMessageException {
        send(GroupRequest.getAvatar(chatID));

        try {
            return FileIOUtil.receive(this::receive);
        } catch (NoEffectException e) {
            return null;
        }
    }
}
