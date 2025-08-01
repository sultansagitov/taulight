package net.result.taulight.message.types;

import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.dto.RoleRequestDTO;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class RoleRequest extends MSGPackMessage<RoleRequestDTO> {
    private RoleRequest(RoleRequestDTO data) {
        super(new Headers().setType(TauMessageTypes.ROLES), data);
    }

    public static @NotNull RoleRequest getRoles(UUID chatID) {
        return new RoleRequest(new RoleRequestDTO(RoleRequestDTO.DataType.GET, chatID, null, null));
    }

    public static @NotNull RoleRequest addRole(UUID chatID, String roleName) {
        return new RoleRequest(new RoleRequestDTO(RoleRequestDTO.DataType.CREATE, chatID, roleName, null));
    }

    public static @NotNull RoleRequest assignRole(UUID chatID, String roleName, String nickname) {
        return new RoleRequest(new RoleRequestDTO(RoleRequestDTO.DataType.ADD, chatID, roleName, nickname));
    }

    public RoleRequest(RawMessage raw) throws DeserializationException, ExpectedMessageException {
        super(raw.expect(TauMessageTypes.ROLES), RoleRequestDTO.class);
    }
}
