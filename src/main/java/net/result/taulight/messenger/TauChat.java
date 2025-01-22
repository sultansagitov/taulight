package net.result.taulight.messenger;

import net.result.sandnode.db.IMember;

import java.util.HashSet;
import java.util.Set;

public class TauChat {
    public final String name;
    public final Set<IMember> members;

    public TauChat(String name) {
        this.name = name;
        this.members = new HashSet<>();
    }
}
