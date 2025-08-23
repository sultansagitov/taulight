package net.result.taulight.message.types;

import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.dto.ChatMemberDTO;
import net.result.taulight.dto.MembersResponseDTO;
import net.result.taulight.dto.RoleDTO;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class MembersResponse extends MSGPackMessage<MembersResponseDTO> {
    public MembersResponse(
            @NotNull Headers headers,
            @NotNull Collection<ChatMemberDTO> records,
            @Nullable Collection<RoleDTO> roles
    ) {
        super(headers.setType(TauMessageTypes.MEMBERS), new MembersResponseDTO(records, roles));
    }

    public MembersResponse(@NotNull Collection<ChatMemberDTO> records, @Nullable Collection<RoleDTO> roles) {
        this(new Headers(), records, roles);
    }

    public MembersResponse(@NotNull RawMessage message) {
        super(message.expect(TauMessageTypes.MEMBERS), MembersResponseDTO.class);
    }
}
