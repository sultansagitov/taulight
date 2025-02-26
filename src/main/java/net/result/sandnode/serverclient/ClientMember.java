package net.result.sandnode.serverclient;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class ClientMember {
    @JsonProperty("member-id")
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

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof ClientMember cm && memberID.equals(cm.memberID);
    }
}
