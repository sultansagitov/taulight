package net.result.taulight.message.types;

import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.dto.CodeResponseDTO;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

public class CodeResponse extends MSGPackMessage<CodeResponseDTO> {
    public CodeResponse(CodeResponseDTO data) {
        this(new Headers(), data);
    }

    public CodeResponse(@NotNull Headers headers, CodeResponseDTO data) {
        super(headers.setType(TauMessageTypes.CODE), data);
    }

    public CodeResponse(@NotNull RawMessage raw) throws DeserializationException, ExpectedMessageException {
        super(raw.expect(TauMessageTypes.CODE), CodeResponseDTO.class);
    }

    public CodeResponseDTO.Check check() {
        return dto().check;
    }
}
