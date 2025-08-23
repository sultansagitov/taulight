package net.result.sandnode.message.types;

import net.result.sandnode.dto.RegisterRequestDTO;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;

public class RegistrationRequest extends MSGPackMessage<RegisterRequestDTO> {

    public RegistrationRequest(@NotNull RawMessage message) {
        super(message.expect(MessageTypes.REG), RegisterRequestDTO.class);
    }

    public RegistrationRequest(RegisterRequestDTO dto) {
        super(new Headers().setType(MessageTypes.REG), dto);
    }
}
