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
import net.result.sandnode.message.types.AvatarRequest;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.util.FileIOUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class AvatarClientChain extends ClientChain {
    private static final Logger LOGGER = LogManager.getLogger(AvatarClientChain.class);

    public AvatarClientChain(SandnodeClient client) {
        super(client);
    }

    public UUID set(String avatarPath) throws UnprocessedMessagesException, InterruptedException,
            UnknownSandnodeErrorException, SandnodeErrorException, ExpectedMessageException, FSException,
            DeserializationException {
        Path path = Paths.get(avatarPath);

        String contentType = URLConnection.guessContentTypeFromName(path.getFileName().toString());
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(path);
        } catch (IOException e) {
            LOGGER.error("Failed to read file at path: {}", avatarPath, e);
            throw new FSException(e);
        }

        IMessage request = AvatarRequest.byType(AvatarRequest.Type.SET);
        FileDTO dto = new FileDTO(null, contentType, bytes);

        send(request);
        FileIOUtil.send(dto, this::send);

        RawMessage raw = queue.take();
        ServerErrorManager.instance().handleError(raw);

        raw.expect(MessageTypes.HAPPY);
        UUIDMessage response = new UUIDMessage(raw);
        return response.uuid;
    }

    public @Nullable FileDTO getMy() throws InterruptedException, ExpectedMessageException,
            UnknownSandnodeErrorException, SandnodeErrorException, UnprocessedMessagesException {
        send(AvatarRequest.byType(AvatarRequest.Type.GET_MY));
        try {
            return FileIOUtil.receive(queue::take);
        } catch (NoEffectException e) {
            return null;
        }
    }

    public @Nullable FileDTO getOf(String nickname) throws InterruptedException, ExpectedMessageException,
            UnknownSandnodeErrorException, SandnodeErrorException, UnprocessedMessagesException {
        AvatarRequest request = AvatarRequest.byType(AvatarRequest.Type.GET_OF);
        request.headers().setValue("nickname", nickname);
        send(request);
        try {
            return FileIOUtil.receive(queue::take);
        } catch (NoEffectException e) {
            return null;
        }
    }
}
