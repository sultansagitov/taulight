package net.result.taulight.db;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;
import java.util.UUID;

public class ReactionEntry extends TaulightObject {
    @JsonProperty("message_id")
    private UUID messageId;
    @JsonProperty("reaction_type_id")
    private UUID reactionTypeId;
    @JsonProperty
    private String nickname;

    @SuppressWarnings("unused")
    public ReactionEntry() {
        super();
    }

    public ReactionEntry(TauDatabase database, UUID id, ZonedDateTime createdAt,
                         UUID messageId, UUID reactionTypeId, String nickname) {
        super(database, id, createdAt);
        this.messageId = messageId;
        this.reactionTypeId = reactionTypeId;
        this.nickname = nickname;
    }

    public ReactionEntry(TauDatabase database, UUID messageId, UUID reactionTypeId, String nickname) {
        super(database);
        this.messageId = messageId;
        this.reactionTypeId = reactionTypeId;
        this.nickname = nickname;
    }

    public UUID messageId() { return messageId; }

    public UUID reactionTypeId() { return reactionTypeId; }

    public String nickname() { return nickname; }
}
