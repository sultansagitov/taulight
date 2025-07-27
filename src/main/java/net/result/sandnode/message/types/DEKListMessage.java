package net.result.sandnode.message.types;

import com.fasterxml.jackson.core.type.TypeReference;
import net.result.sandnode.dto.DEKResponseDTO;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;

import java.util.List;

public class DEKListMessage extends MSGPackMessage<List<DEKResponseDTO>> {
    public DEKListMessage(List<DEKResponseDTO> list) {
        super(new Headers().setType(MessageTypes.DEK), list);
    }

    public DEKListMessage(RawMessage raw) throws DeserializationException, ExpectedMessageException {
        super(raw.expect(MessageTypes.DEK), new TypeReference<>() {});
    }

    public List<DEKResponseDTO> list() {
        return object;
    }
}