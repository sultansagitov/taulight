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

public class Dialogesponse extends MSGPackMessage<Dialogesponse.Data> {
    protected static class Data {
        @JsonProperty("member-id")
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

    public Dialogesponse(@NotNull Headers headers, String memberID, UUID id) {
        super(headers.setType(TauMessageTypes.DIALOG), new Data(memberID, id));
    }

    public Dialogesponse(String memberID, UUID id) {
        this(new Headers(), memberID, id);
    }

    public Dialogesponse(@NotNull RawMessage message) throws ExpectedMessageException, DeserializationException {
        super(message.expect(TauMessageTypes.DIALOG), Dialogesponse.Data.class);
    }

    public UUID getChatID() {
        return object.id;
    }
}
