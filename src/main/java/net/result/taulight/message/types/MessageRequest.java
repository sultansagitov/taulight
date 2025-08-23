package net.result.taulight.message.types;

import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.dto.MessageRequestDTO;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class MessageRequest extends MSGPackMessage<MessageRequestDTO> {
    public MessageRequest(@NotNull Headers headers, UUID chatID, int index, int size) {
        super(headers.setType(TauMessageTypes.MESSAGE), new MessageRequestDTO(chatID, index, size));
    }

    public MessageRequest(UUID chatID, int index, int size) {
        this(new Headers(), chatID, index, size);
    }

    public MessageRequest(@NotNull RawMessage raw) {
        super(raw.expect(TauMessageTypes.MESSAGE), MessageRequestDTO.class);
    }
}
