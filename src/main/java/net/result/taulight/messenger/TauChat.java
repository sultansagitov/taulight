package net.result.taulight.messenger;

import net.result.sandnode.db.Member;
import net.result.sandnode.group.GroupManager;
import net.result.taulight.group.TauChatGroup;

import java.util.Collection;
import java.util.HashSet;

public class TauChat {
    private final String id;
    private final Collection<Member> members;
    public final TauChatGroup group;

    public TauChat(String id, GroupManager manager) {
        this.id = id;
        group = new TauChatGroup(id);
        manager.add(group);
        this.members = new HashSet<>();
    }

    public String getID() {
        return id;
    }

    public Collection<Member> getMembers() {
        return members;
    }
}
