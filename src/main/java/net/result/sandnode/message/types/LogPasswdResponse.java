package net.result.sandnode.message.types;

import net.result.sandnode.dto.LogPasswdResponseDTO;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class LogPasswdResponse extends MSGPackMessage<LogPasswdResponseDTO> {
    public LogPasswdResponse(@NotNull Headers headers, String token, UUID keyID) {
        super(headers.setType(MessageTypes.LOG_PASSWD), new LogPasswdResponseDTO(token, keyID));
    }

    public LogPasswdResponse(String token, UUID keyID) {
        this(new Headers(), token, keyID);
    }

    public LogPasswdResponse(@NotNull RawMessage raw) throws ExpectedMessageException, DeserializationException {
        super(raw.expect(MessageTypes.LOG_PASSWD), LogPasswdResponseDTO.class);
    }

    public LogPasswdResponseDTO dto() {
        return object;
    }
}
