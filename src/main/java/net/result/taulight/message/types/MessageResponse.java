package net.result.taulight.message.types;

import com.fasterxml.jackson.core.type.TypeReference;
import net.result.sandnode.dto.PaginatedDTO;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MessageResponse extends MSGPackMessage<PaginatedDTO<ChatMessageViewDTO>> {
    public MessageResponse(@NotNull Headers headers, long totalCount, List<ChatMessageViewDTO> messages) {
        super(headers.setType(TauMessageTypes.MESSAGE), new PaginatedDTO<>(totalCount, messages));
    }

    public MessageResponse(long totalCount, List<ChatMessageViewDTO> messages) {
        this(new Headers(), totalCount, messages);
    }

    public MessageResponse(@NotNull RawMessage raw) throws ExpectedMessageException, DeserializationException {
        super(raw.expect(TauMessageTypes.MESSAGE), new TypeReference<>() {});
    }

    public long getCount() {
        return object.totalCount;
    }

    public List<ChatMessageViewDTO> getMessages() {
        return object.objects;
    }
}
