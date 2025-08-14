package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Data Transfer Object representing a chat member's nickname and online status.
 */
public class ChatMemberDTO {
    /** Nickname of the member. */
    @JsonProperty
    public String nickname;
    /** Online status of the member. */
    @JsonProperty
    public MemberStatus status;
    /** Roles of the member in current chat */
    @JsonProperty
    public @Nullable List<String> roles;

    /** Default constructor. */
    @SuppressWarnings("unused")
    public ChatMemberDTO() {}

    public ChatMemberDTO(String nickname, MemberStatus status, @Nullable List<String> roles) {
        this.nickname = nickname;
        this.status = status;
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "<ChatMemberDTO %s %s>".formatted(nickname, status.name());
    }
}
