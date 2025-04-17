package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.taulight.db.ReactionEntryEntity;
import org.jetbrains.annotations.NotNull;

public class ReactionDTO {
    @JsonProperty
    public boolean isReact;
    @JsonProperty
    public String nickname;
    @JsonProperty("package-name")
    public String packageName;
    @JsonProperty
    public String reaction;

    @SuppressWarnings("unused")
    public ReactionDTO() {}

    public ReactionDTO(boolean isReact, String nickname, String packageName, String reaction) {
        this.isReact = isReact;
        this.nickname = nickname;
        this.packageName = packageName;
        this.reaction = reaction;
    }

    public ReactionDTO(boolean isReact, @NotNull ReactionEntryEntity entry) {
        this(
                isReact,
                entry.member().member().nickname(),
                entry.reactionType().reactionPackage().name(),
                entry.reactionType().name()
        );
    }

    @Override
    public String toString() {
        String s = isReact ? "react" : "unreact";
        return "<ReactionDTO %s %s %s:%s>".formatted(s, nickname, packageName, reaction);
    }
}
