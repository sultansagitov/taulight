package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.db.MemberEntity;
import org.jetbrains.annotations.NotNull;

/**
 * Data Transfer Object representing a chat member's nickname and online status.
 */
public class ChatMemberDTO {
    /** Possible online statuses for a chat member. */
    public enum Status {ONLINE, OFFLINE, HIDDEN}

    /** Nickname of the member. */
    @JsonProperty
    public String nickname;
    /** Online status of the member. */
    @JsonProperty
    public Status status = Status.OFFLINE;

    /** Default constructor. */
    @SuppressWarnings("unused")
    public ChatMemberDTO() {}

    /**
     * Constructs a ChatMemberDTO from a {@link MemberEntity}.
     *
     * @param member the member entity
     */
    public ChatMemberDTO(@NotNull MemberEntity member) {
        nickname = member.nickname();
    }

    @Override
    public String toString() {
        return "<ChatMemberDTO %s %s>".formatted(nickname, status.name());
    }
}
