package net.result.sandnode.message.types;

import com.fasterxml.jackson.core.type.TypeReference;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.dto.DEKDTO;

import java.util.List;

public class DEKListMessage extends MSGPackMessage<List<DEKDTO>> {
    public DEKListMessage(List<DEKDTO> list) {
        super(new Headers().setType(MessageTypes.DEK), list);
    }

    public DEKListMessage(RawMessage raw) throws DeserializationException, ExpectedMessageException {
        super(raw.expect(MessageTypes.DEK), new TypeReference<>() {});
    }

    public List<DEKDTO> list() {
        return object;
    }
}