package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.db.MemberEntity;
import org.jetbrains.annotations.NotNull;

public class ChatMemberDTO {
    public enum Status {ONLINE, OFFLINE, HIDDEN}

    @JsonProperty
    public String nickname;
    @JsonProperty
    public Status status = Status.OFFLINE;

    @SuppressWarnings("unused")
    public ChatMemberDTO() {}

    public ChatMemberDTO(@NotNull MemberEntity member) {
        nickname = member.nickname();
    }

    @Override
    public String toString() {
        return "<ChatMemberDTO %s %s>".formatted(nickname, status.name());
    }
}
