package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.dto.FileDTO;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.UUIDMessage;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.util.FileIOUtil;
import net.result.taulight.message.types.MessageFileRequest;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

public class MessageFileClientChain extends ClientChain {
    public MessageFileClientChain(SandnodeClient client) {
        super(client);
    }

    public UUID upload(UUID chatID, String pathString, String name)
            throws FSException, UnprocessedMessagesException, InterruptedException, UnknownSandnodeErrorException,
            SandnodeErrorException, DeserializationException, ExpectedMessageException {
        var path = Paths.get(pathString);
        var contentType = URLConnection.guessContentTypeFromName(path.getFileName().toString());

        byte[] bytes;
        try {
            bytes = Files.readAllBytes(path);
        } catch (IOException e) {
            throw new FSException(e);
        }

        var dto = new FileDTO(null, contentType, bytes);

        var request = MessageFileRequest.uploadTo(chatID, name);

        send(request);
        FileIOUtil.send(dto, this::send);

        var raw = receive();

        raw.expect(MessageTypes.HAPPY);
        return new UUIDMessage(raw).uuid;
    }

    public FileDTO download(UUID fileID) throws InterruptedException, UnknownSandnodeErrorException,
            SandnodeErrorException, ExpectedMessageException, UnprocessedMessagesException, DeserializationException {
        MessageFileRequest request = MessageFileRequest.download(fileID);
        send(request);

        FileDTO dto = FileIOUtil.receive(this::receive);
        System.out.println("File downloaded successfully.");

        return dto;
    }
}
