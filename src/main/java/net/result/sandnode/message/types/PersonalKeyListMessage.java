package net.result.sandnode.message.types;

import com.fasterxml.jackson.core.type.TypeReference;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import net.result.taulight.dto.PersonalKeyDTO;

import java.util.List;

public class PersonalKeyListMessage extends MSGPackMessage<List<PersonalKeyDTO>> {
    public PersonalKeyListMessage(List<PersonalKeyDTO> list) {
        super(new Headers().setType(MessageTypes.PERSONAL_KEY), list);
    }

    public PersonalKeyListMessage(RawMessage raw) throws DeserializationException, ExpectedMessageException {
        super(raw.expect(MessageTypes.PERSONAL_KEY), new TypeReference<>() {});
    }

    public List<PersonalKeyDTO> list() {
        return object;
    }
}