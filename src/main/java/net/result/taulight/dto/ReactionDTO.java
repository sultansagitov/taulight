package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.taulight.db.ReactionEntryEntity;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Data Transfer Object representing a reaction event on a chat message.
 */
public class ReactionDTO {
    /** Indicates whether this is a reaction (true) or an unreaction (false). */
    @JsonProperty
    public boolean isReact;
    /** Nickname of the reacting member. */
    @JsonProperty
    public String nickname;
    /** ID of the chat where the reaction occurred. */
    @JsonProperty
    public UUID chatID;
    /** ID of the message that was reacted to. */
    @JsonProperty
    public UUID messageID;
    /** Package name of the reaction type. */
    @JsonProperty("package-name")
    public String packageName;
    /** Name of the specific reaction. */
    @JsonProperty
    public String reaction;

    /** Default constructor. */
    @SuppressWarnings("unused")
    public ReactionDTO() {}

    /**
     * Constructs a ReactionDTO manually.
     *
     * @param isReact whether this is a react or unreact event
     * @param nickname the member nickname
     * @param chatID the chat ID
     * @param messageID the message ID
     * @param packageName the package of the reaction
     * @param reaction the reaction type
     */
    public ReactionDTO(
            boolean isReact,
            String nickname,
            UUID chatID,
            UUID messageID,
            String packageName,
            String reaction
    ) {
        this.isReact = isReact;
        this.nickname = nickname;
        this.chatID = chatID;
        this.messageID = messageID;
        this.packageName = packageName;
        this.reaction = reaction;
    }

    /**
     * Constructs a ReactionDTO from a {@link ReactionEntryEntity}.
     *
     * @param isReact true if adding a reaction, false if removing
     * @param entry the reaction entry entity
     */
    public ReactionDTO(boolean isReact, @NotNull ReactionEntryEntity entry) {
        this(
                isReact,
                entry.member().member().nickname(),
                entry.message().chat().id(),
                entry.message().id(),
                entry.reactionType().reactionPackage().name(),
                entry.reactionType().name()
        );
    }

    @Override
    public String toString() {
        String s = isReact ? "react" : "unreact";
        return "<ReactionDTO %s %s %s %s:%s>".formatted(s, nickname, chatID, packageName, reaction);
    }
}
