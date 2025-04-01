package net.result.taulight.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.db.Member;
import org.jetbrains.annotations.NotNull;

public class MemberRecord {
    public enum Status {ONLINE, OFFLINE, HIDDEN}

    @JsonProperty
    public String nickname;
    @JsonProperty
    public Status status = Status.OFFLINE;

    @SuppressWarnings("unused")
    public MemberRecord() {}

    public MemberRecord(@NotNull Member member) {
        nickname = member.nickname();
    }

    @Override
    public String toString() {
        return "<MemberRecord %s %s>".formatted(nickname, status.name());
    }
}
