package net.result.taulight.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.message.TauMessageTypes;

import java.util.UUID;

public class ReactionRequest extends MSGPackMessage<ReactionRequest.Data> {
    protected static class Data {
        @JsonProperty("message-id")
        public UUID messageID;

        @JsonProperty("reaction")
        public String reaction;

        @JsonProperty("react")
        public boolean react = true;

        @SuppressWarnings("unused")
        public Data() {}

        public Data(UUID messageID, String reaction, boolean react) {
            this.messageID = messageID;
            this.reaction = reaction;
            this.react = react;
        }
    }

    private ReactionRequest(Headers headers, Data data) {
        super(headers.setType(TauMessageTypes.REACTION), data);
    }

    private ReactionRequest(Data data) {
        this(new Headers(), data);
    }

    public ReactionRequest(RawMessage raw) throws DeserializationException, ExpectedMessageException {
        super(raw.expect(TauMessageTypes.REACTION), Data.class);
    }

    public static ReactionRequest react(UUID messageID, String reaction) {
        return new ReactionRequest(new Data(messageID, reaction, true));
    }

    public static ReactionRequest unreact(UUID messageID, String reaction) {
        return new ReactionRequest(new Data(messageID, reaction, false));
    }

    public UUID getMessageID() {
        return object.messageID;
    }

    public String getReactionType() {
        return object.reaction;
    }

    public boolean isReact() {
        return object.react;
    }
}