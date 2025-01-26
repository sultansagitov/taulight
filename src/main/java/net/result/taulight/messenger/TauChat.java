package net.result.taulight.messenger;

import net.result.sandnode.db.Member;

import java.util.Collection;
import java.util.HashSet;

public class TauChat {
    private final String id;
    private final Collection<Member> members;

    public TauChat(String id) {
        this.id = id;
        this.members = new HashSet<>();
    }

    public String getID() {
        return id;
    }

    public Collection<Member> getMembers() {
        return members;
    }
}
