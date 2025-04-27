package net.result.taulight.message.types;

import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.dto.RolesDTO;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

public class RoleResponse extends MSGPackMessage<RolesDTO> {
    public RoleResponse(RolesDTO dto) {
        this(new Headers(), dto);
    }

    public RoleResponse(@NotNull Headers headers, RolesDTO dto) {
        super(headers.setType(TauMessageTypes.ROLES), dto);
    }

    public RoleResponse(@NotNull RawMessage raw) throws DeserializationException, ExpectedMessageException {
        super(raw.expect(TauMessageTypes.ROLES), RolesDTO.class);
    }

    public RolesDTO roles() {
        return object;
    }
}
