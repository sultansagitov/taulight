package net.result.sandnode.serverclient;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.db.IMember;

public final class ClientMember {
    @JsonProperty
    public String memberID;

    public ClientMember() {}
    public ClientMember(String memberID) {
        this.memberID = memberID;
    }

    public static ClientMember of(IMember member) {
        return new ClientMember(member.getID());
    }
}
