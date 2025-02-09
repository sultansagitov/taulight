package net.result.sandnode.serverclient;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class ClientMember {
    @JsonProperty
    public String memberID;

    @SuppressWarnings("unused")
    public ClientMember() {}
    public ClientMember(String memberID) {
        this.memberID = memberID;
    }

    @Override
    public String toString() {
        return "<%s %s>".formatted(getClass().getSimpleName(), memberID);
    }
}
