package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.dto.FileDTO;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.UUIDMessage;
import net.result.sandnode.message.types.FileMessage;
import net.result.sandnode.message.types.WhoAmIRequest;
import net.result.sandnode.message.types.WhoAmIResponse;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.SandnodeClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class WhoAmIClientChain extends ClientChain {

    private static final Logger LOGGER = LogManager.getLogger(WhoAmIClientChain.class);

    public WhoAmIClientChain(SandnodeClient client) {
        super(client);
    }

    public synchronized String getNickname() throws InterruptedException, ExpectedMessageException,
            UnknownSandnodeErrorException, SandnodeErrorException, UnprocessedMessagesException {
        send(WhoAmIRequest.byType(WhoAmIRequest.Type.NICKNAME));

        RawMessage raw = queue.take();
        ServerErrorManager.instance().handleError(raw);

        return new WhoAmIResponse(raw).getID();
    }

    public synchronized @Nullable FileDTO getAvatar() throws InterruptedException, ExpectedMessageException,
            UnknownSandnodeErrorException, SandnodeErrorException, UnprocessedMessagesException {
        send(WhoAmIRequest.byType(WhoAmIRequest.Type.GET_AVATAR));
        try {
            RawMessage raw = queue.take();
            ServerErrorManager.instance().handleError(raw);
            return new FileMessage(raw).dto();
        } catch (NoEffectException e) {
            return null;
        }
    }

    public UUID setAvatar(String avatarPath) throws UnprocessedMessagesException, InterruptedException,
            UnknownSandnodeErrorException, SandnodeErrorException, ExpectedMessageException, FSException, DeserializationException {
        Path path = Paths.get(avatarPath);

        String contentType = URLConnection.guessContentTypeFromName(path.getFileName().toString());
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(path);
        } catch (IOException e) {
            LOGGER.error("Failed to read file at path: {}", avatarPath, e);
            throw new FSException(e);
        }

        IMessage request = WhoAmIRequest.byType(WhoAmIRequest.Type.SET_AVATAR);
        FileMessage fileMessage = new FileMessage(new FileDTO(null, contentType, bytes));

        send(request);
        send(fileMessage);

        RawMessage raw = queue.take();
        ServerErrorManager.instance().handleError(raw);

        raw.expect(MessageTypes.HAPPY);
        UUIDMessage response = new UUIDMessage(raw);
        return response.uuid;
    }
}
