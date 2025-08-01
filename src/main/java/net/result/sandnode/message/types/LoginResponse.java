package net.result.sandnode.message.types;

import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.dto.LoginResponseDTO;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;

public class LoginResponse extends MSGPackMessage<LoginResponseDTO> {
    public LoginResponse(@NotNull Headers headers, @NotNull MemberEntity member) {
        super(headers.setType(MessageTypes.LOGIN), new LoginResponseDTO(member.nickname()));
    }

    public LoginResponse(MemberEntity member) {
        this(new Headers(), member);
    }

    public LoginResponse(Message message) throws DeserializationException {
        super(message, LoginResponseDTO.class);
    }
}
