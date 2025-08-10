package net.result.taulight.message.types;

import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.dto.CodeRequestDTO;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

public class CodeRequest extends MSGPackMessage<CodeRequestDTO> {
    public CodeRequest(CodeRequestDTO dto) {
        this(new Headers(), dto);
    }

    public CodeRequest(@NotNull Headers headers, CodeRequestDTO dto) {
        super(headers.setType(TauMessageTypes.CODE), dto);
    }

    public CodeRequest(@NotNull RawMessage raw) throws ExpectedMessageException, DeserializationException {
        super(raw.expect(TauMessageTypes.CODE), CodeRequestDTO.class);
    }

    public CodeRequestDTO.Check check() {
        return dto().check;
    }

    public CodeRequestDTO.Use use() {
        return dto().use;
    }

    public CodeRequestDTO.GroupCodes groupCodes() {
        return dto().groupCodes;
    }

    public boolean myCodes() {
        return dto().myCodes;
    }
}
