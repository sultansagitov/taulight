package net.result.sandnode.serverclient;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.db.Member;

public final class ClientMember {
    @JsonProperty
    public String memberID;

    @SuppressWarnings("unused")
    public ClientMember() {}
    public ClientMember(String memberID) {
        this.memberID = memberID;
    }

    public static ClientMember of(Member member) {
        return new ClientMember(member.getID());
    }
}
