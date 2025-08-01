package net.result.taulight.message.types;

import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.dto.TauMemberSettingsResponseDTO;
import net.result.taulight.message.TauMessageTypes;

public class TauMemberSettingsResponse extends MSGPackMessage<TauMemberSettingsResponseDTO> {
    public TauMemberSettingsResponse(TauMemberSettingsResponseDTO dto) {
        super(new Headers().setType(TauMessageTypes.TAU_SETTINGS), dto);
    }

    public TauMemberSettingsResponse(RawMessage raw) throws DeserializationException, ExpectedMessageException {
        super(raw.expect(TauMessageTypes.TAU_SETTINGS), TauMemberSettingsResponseDTO.class);
    }
}
