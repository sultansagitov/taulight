package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.dto.FileDTO;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.UUIDMessage;
import net.result.sandnode.message.types.FileMessage;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.message.types.MessageFileRequest;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class MessageFileClientChain extends ClientChain {
    public MessageFileClientChain(SandnodeClient client) {
        super(client);
    }

    public UUID uploadFile(UUID chatID, String pathString)
            throws FSException, UnprocessedMessagesException, InterruptedException, UnknownSandnodeErrorException,
            SandnodeErrorException, DeserializationException, ExpectedMessageException {
        Path path = Paths.get(pathString);
        String contentType = URLConnection.guessContentTypeFromName(path.getFileName().toString());

        byte[] bytes;
        try {
            bytes = Files.readAllBytes(path);
        } catch (IOException e) {
            throw new FSException(e);
        }

        FileDTO dto = new FileDTO(null, contentType, bytes);

        MessageFileRequest request = MessageFileRequest.uploadTo(chatID);
        FileMessage fileMessage = new FileMessage(dto);

        send(request);
        send(fileMessage);

        RawMessage raw = queue.take();
        ServerErrorManager.instance().handleError(raw);

        raw.expect(MessageTypes.HAPPY);
        return new UUIDMessage(raw).uuid;
    }
}
