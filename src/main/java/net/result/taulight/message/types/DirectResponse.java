package net.result.taulight.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

public class DirectResponse extends MSGPackMessage<DirectResponse.Data> {
    protected static class Data {
        @JsonProperty
        public String memberID;

        @JsonProperty
        public String id;

        @SuppressWarnings("unused")
        public Data() {}
        public Data(String memberID, String id) {
            this.memberID = memberID;
            this.id = id;
        }
    }

    public DirectResponse(@NotNull Headers headers, String memberID, String id) {
        super(headers.setType(TauMessageTypes.DIRECT), new Data(memberID, id));
    }

    public DirectResponse(String memberID, String id) {
        this(new Headers(), memberID, id);
    }

    public DirectResponse(@NotNull RawMessage message) throws ExpectedMessageException, DeserializationException {
        super(message.expect(TauMessageTypes.DIRECT), DirectResponse.Data.class);
    }

    public String getMemberID() {
        return object.memberID;
    }

    public String getChatID() {
        return object.id;
    }
}
