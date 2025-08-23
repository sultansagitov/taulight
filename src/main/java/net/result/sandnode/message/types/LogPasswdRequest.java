package net.result.sandnode.message.types;

import net.result.sandnode.dto.LogPasswdRequestDTO;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;

public class LogPasswdRequest extends MSGPackMessage<LogPasswdRequestDTO> {
    public LogPasswdRequest(@NotNull Headers headers, String nickname, String password, String device) {
        super(headers.setType(MessageTypes.LOG_PASSWD), new LogPasswdRequestDTO(nickname, password, device));
    }

    public LogPasswdRequest(String nickname, String password, String device) {
        this(new Headers(), nickname, password, device);
    }

    public LogPasswdRequest(RawMessage raw) {
        super(raw.expect(MessageTypes.LOG_PASSWD), LogPasswdRequestDTO.class);
    }
}
