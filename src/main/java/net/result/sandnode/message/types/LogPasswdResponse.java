package net.result.sandnode.message.types;

import net.result.sandnode.dto.LogPasswdResponseDTO;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;

public class LogPasswdResponse extends MSGPackMessage<LogPasswdResponseDTO> {
    public LogPasswdResponse(@NotNull Headers headers, String token) {
        super(headers.setType(MessageTypes.LOG_PASSWD), new LogPasswdResponseDTO(token));
    }

    public LogPasswdResponse(String token) {
        this(new Headers(), token);
    }

    public LogPasswdResponse(@NotNull RawMessage raw) {
        super(raw.expect(MessageTypes.LOG_PASSWD), LogPasswdResponseDTO.class);
    }
}
