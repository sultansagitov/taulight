package net.result.sandnode.message.types;

import net.result.sandnode.dto.RegistrationResponseDTO;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class RegistrationResponse extends MSGPackMessage<RegistrationResponseDTO> {

    public RegistrationResponse(@NotNull RawMessage message) throws ExpectedMessageException, DeserializationException {
        super(message.expect(MessageTypes.REG), RegistrationResponseDTO.class);
    }

    public RegistrationResponse(@NotNull Headers headers, @NotNull String token, UUID keyID) {
        super(headers.setType(MessageTypes.REG), new RegistrationResponseDTO(token, keyID));
    }

    public RegistrationResponse(@NotNull String token, UUID keyID) {
        this(new Headers(), token, keyID);
    }

    public RegistrationResponseDTO dto() {
        return object;
    }
}
