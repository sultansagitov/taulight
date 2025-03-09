package net.result.taulight.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.db.Member;

public class MemberRecord {
    @JsonProperty
    public String id;

    @SuppressWarnings("unused")
    public MemberRecord() {}
    public MemberRecord(Member m) {
        id = m.id();
    }

    @Override
    public String toString() {
        return "<MemberRecord %s>".formatted(id);
    }
}
