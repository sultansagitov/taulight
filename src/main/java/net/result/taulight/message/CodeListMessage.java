package net.result.taulight.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.dto.CodeDTO;

import java.util.Collection;

public class CodeListMessage extends MSGPackMessage<CodeListMessage.Data> {
    protected static class Data {
        @JsonProperty
        public Collection<CodeDTO> codes;

        public Data() {}

        public Data(Collection<CodeDTO> codes) {
            this.codes = codes;
        }
    }

    public CodeListMessage(Headers headers, Collection<CodeDTO> codes) {
        super(headers, new Data(codes));
    }

    public CodeListMessage(RawMessage raw) {
        super(raw, Data.class);
    }

    public Collection<CodeDTO> codes() {
        return object.codes;
    }
}
