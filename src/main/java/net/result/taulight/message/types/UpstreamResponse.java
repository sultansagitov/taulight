package net.result.taulight.message.types;

import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.dto.UpstreamResponseDTO;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

public class UpstreamResponse extends MSGPackMessage<UpstreamResponseDTO> {
    public UpstreamResponse(@NotNull Headers headers, @NotNull ChatMessageViewDTO view) {
        super(headers.setType(TauMessageTypes.UPSTREAM), new UpstreamResponseDTO(view.id, view.creationDate));
    }

    public UpstreamResponse(ChatMessageViewDTO view) {
        this(new Headers(), view);
    }

    public UpstreamResponse(RawMessage raw) {
        super(raw.expect(TauMessageTypes.UPSTREAM), UpstreamResponseDTO.class);
    }
}
