package net.result.taulight.messenger;

import net.result.sandnode.db.IMember;

import java.util.Collection;
import java.util.HashSet;

public class TauChat {
    private final String id;
    private final Collection<IMember> members;

    public TauChat(String id) {
        this.id = id;
        this.members = new HashSet<>();
    }

    public String getID() {
        return id;
    }

    public Collection<IMember> getMembers() {
        return members;
    }
}
