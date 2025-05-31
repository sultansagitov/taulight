package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.db.MemberEntity;
import net.result.taulight.db.TauMemberEntity;
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
    public MemberStatus status = MemberStatus.OFFLINE;
    /** Roles of the member in current chat */
    @JsonProperty
    public @Nullable List<String> roles;

    /** Default constructor. */
    @SuppressWarnings("unused")
    public ChatMemberDTO() {}

    /**
     * Constructs a ChatMemberDTO from a {@link MemberEntity}.
     *
     * @param member the member entity
     * @param roles  roles of member in current chat
     */
    public ChatMemberDTO(TauMemberEntity member, @Nullable List<String> roles) {
        nickname = member.member().nickname();
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "<ChatMemberDTO %s %s>".formatted(nickname, status.name());
    }
}
