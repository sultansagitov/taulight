package net.result.sandnode.message.types;

import net.result.sandnode.dto.FileChunkDTO;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.BaseMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class FileMessage extends BaseMessage {
    private final FileChunkDTO chunk;

    public FileMessage(@NotNull FileChunkDTO chunk) {
        this(new Headers(), chunk);
    }

    public FileMessage(@NotNull Headers headers, @NotNull FileChunkDTO chunk) {
        super(headers
                .setType(MessageTypes.FILE)
                .setValue("content-type", chunk.contentType())
                .setValue("id", chunk.id().toString())
                .setValue("sequence", String.valueOf(chunk.sequence()))
                .setValue("total-chunks", String.valueOf(chunk.totalChunks()))
        );
        this.chunk = chunk;
    }

    public FileMessage(@NotNull RawMessage raw) throws ExpectedMessageException {
        super(raw.expect(MessageTypes.FILE).headers());

        UUID id = UUID.fromString(headers().getValue("id"));
        String contentType = headers().getValue("content-type");
        int sequence = Integer.parseInt(headers().getValue("sequence"));
        int totalChunks = Integer.parseInt(headers().getValue("total-chunks"));

        this.chunk = new FileChunkDTO(id, contentType, raw.getBody(), sequence, totalChunks);
    }

    public FileChunkDTO chunk() {
        return chunk;
    }

    @Override
    public byte[] getBody() {
        return chunk.body();
    }
}
