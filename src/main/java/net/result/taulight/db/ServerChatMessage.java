package net.result.taulight.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.db.SandnodeObject;

public class ServerChatMessage extends SandnodeObject {
    @JsonProperty
    private ChatMessage message;

    public ChatMessage message() {
        return message;
    }

    public void setChatMessage(ChatMessage message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "<ServerChatMessage id=%s created=%s message=%s>".formatted(id(), getCreationDate(), message);
    }
}
