package net.result.taulight.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DirectResponse extends MSGPackMessage<DirectResponse.Data> {
    protected static class Data {
        @JsonProperty
        public String memberID;
        @JsonProperty
        public UUID id;

        @SuppressWarnings("unused")
        public Data() {}
        public Data(String memberID, UUID id) {
            this.memberID = memberID;
            this.id = id;
        }
    }

    public DirectResponse(@NotNull Headers headers, String memberID, UUID id) {
        super(headers.setType(TauMessageTypes.DIRECT), new Data(memberID, id));
    }

    public DirectResponse(String memberID, UUID id) {
        this(new Headers(), memberID, id);
    }

    public DirectResponse(@NotNull RawMessage message) throws ExpectedMessageException, DeserializationException {
        super(message.expect(TauMessageTypes.DIRECT), DirectResponse.Data.class);
    }

    public UUID getChatID() {
        return object.id;
    }
}
