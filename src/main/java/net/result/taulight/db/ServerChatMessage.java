package net.result.taulight.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DatabaseException;
import net.result.taulight.exception.AlreadyExistingRecordException;

public class ServerChatMessage extends TaulightObject {
    @JsonProperty
    private ChatMessage message;

    public ServerChatMessage() {
        super();
    }

    public ServerChatMessage(TauDatabase database) {
        super(database);
    }

    public ChatMessage message() {
        return message;
    }

    public void setChatMessage(ChatMessage message) {
        this.message = message;
    }

    public void save() throws AlreadyExistingRecordException, DatabaseException {
        database().saveMessage(this);
    }

    @Override
    public String toString() {
        return "<ServerChatMessage id=%s created=%s message=%s>".formatted(id(), getCreationDate(), message);
    }
}
