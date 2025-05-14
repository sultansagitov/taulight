package net.result.sandnode.message.types;

import net.result.sandnode.dto.FileDTO;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;

public class FileMessage extends Message {
    private final FileDTO dto;

    public FileMessage(@NotNull FileDTO dto) {
        this(new Headers(), dto);
    }

    public FileMessage(@NotNull Headers headers, @NotNull FileDTO dto) {
        super(headers.setType(MessageTypes.FILE).setValue("content-type", dto.contentType()));
        this.dto = dto;
    }

    public FileMessage(@NotNull RawMessage raw) throws ExpectedMessageException {
        super(raw.expect(MessageTypes.FILE).headers());
        dto = new FileDTO(headers().getValue("content-type"), raw.getBody());
    }

    public FileDTO dto() {
        return dto;
    }

    @Override
    public byte[] getBody() {
        return dto.body();
    }
}
