package net.result.taulight.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.code.TauCode;

import java.util.Collection;

public class CodeListMessage extends MSGPackMessage<CodeListMessage.Data> {
    protected static class Data {
        @JsonProperty
        public Collection<TauCode> codes;

        @SuppressWarnings("unused")
        public Data() {}

        public Data(Collection<TauCode> codes) {
            this.codes = codes;
        }
    }

    public CodeListMessage(Headers headers, Collection<TauCode> codes) {
        super(headers, new Data(codes));
    }

    public CodeListMessage(RawMessage raw) throws DeserializationException {
        super(raw, Data.class);
    }

    public Collection<TauCode> codes() {
        return object.codes;
    }
}
